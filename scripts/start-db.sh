#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_DATA="${PREFIX}/var/lib/mysql"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
MYSQL_PORT="${NSO_DB_PORT:-3306}"
MYSQL_CNF="${ROOT_DIR}/.termux/mariadb.cnf"
PID_FILE="${ROOT_DIR}/.termux/mariadb.pid"
SAFE_PID_FILE="${ROOT_DIR}/.termux/mariadbd-safe.pid"
SOCKET="${MYSQL_RUN}/mysqld.sock"
LOG_FILE="${ROOT_DIR}/logs/mariadb.log"
LOCK_DIR="${ROOT_DIR}/.termux/mariadb-start.lock"
LOCK_OWNER_FILE="${LOCK_DIR}/pid"

mkdir -p "${MYSQL_RUN}" "${ROOT_DIR}/.termux" "${ROOT_DIR}/logs"

if [ ! -d "${MYSQL_DATA}/mysql" ]; then
  printf '%s\n' 'Chưa khởi tạo MariaDB. Hãy chạy lại install.sh hoặc scripts/setup-termux.sh.' >&2
  exit 1
fi

if [ ! -f "${MYSQL_CNF}" ]; then
  printf '%s\n' 'Thiếu cấu hình MariaDB. Hãy chạy lại scripts/setup-termux.sh.' >&2
  exit 1
fi

db_ready() {
  [ -S "${SOCKET}" ] && mariadb-admin --defaults-file="${MYSQL_CNF}" ping >/dev/null 2>&1
}

release_lock() { rm -rf "${LOCK_DIR}" 2>/dev/null || true; }
LOCK_ACQUIRED=0
if mkdir "${LOCK_DIR}" 2>/dev/null; then
  LOCK_ACQUIRED=1
else
  LOCK_OWNER="$(cat "${LOCK_OWNER_FILE}" 2>/dev/null || true)"
  if [ -n "${LOCK_OWNER}" ] && ! kill -0 "${LOCK_OWNER}" 2>/dev/null; then
    printf '%s\n' 'Phát hiện MariaDB start lock cũ sau lần chạy bị ngắt; dọn lock stale rồi thử lại.'
    rm -rf "${LOCK_DIR}"
    if mkdir "${LOCK_DIR}" 2>/dev/null; then LOCK_ACQUIRED=1; fi
  fi
fi
if [ "${LOCK_ACQUIRED}" -ne 1 ]; then
  printf '%s\n' 'Một tiến trình khác đang khởi động MariaDB; chờ health-check thay vì chạy chồng instance.'
  for _ in $(seq 1 30); do
    if db_ready; then printf '%s\n' 'MariaDB đã sẵn sàng từ tiến trình khác.'; exit 0; fi
    sleep 1
  done
  printf '%s\n' 'MariaDB start lock không được giải phóng; không khởi động thêm instance để tránh lỗi Aria/InnoDB lock.' >&2
  exit 1
fi
printf '%s\n' "$$" > "${LOCK_OWNER_FILE}"
trap release_lock EXIT

if db_ready; then
  printf '%s\n' 'MariaDB đang chạy.'
  exit 0
fi

known_pids() {
  local pid cmd candidate
  for candidate in "${PID_FILE}" "${SAFE_PID_FILE}"; do
    if [ -f "${candidate}" ]; then
      pid="$(cat "${candidate}" 2>/dev/null || true)"
      if [ -n "${pid}" ] && kill -0 "${pid}" 2>/dev/null; then
        cmd="$(tr '\0' ' ' < "/proc/${pid}/cmdline" 2>/dev/null || true)"
        case "${cmd}" in
          *mariadbd*"${MYSQL_DATA}"*|*mysqld*"${MYSQL_DATA}"*|*mariadbd*"${MYSQL_CNF}"*|*mysqld*"${MYSQL_CNF}"*) printf '%s\n' "${pid}" ;;
        esac
      fi
    fi
  done
  if command -v pgrep >/dev/null 2>&1; then
    while IFS= read -r pid; do
      cmd="$(tr '\0' ' ' < "/proc/${pid}/cmdline" 2>/dev/null || true)"
      case "${cmd}" in
        *"${MYSQL_DATA}"*|*"${MYSQL_CNF}"*) printf '%s\n' "${pid}" ;;
      esac
    done < <(pgrep -f 'mariadbd|mysqld' 2>/dev/null || true)
  fi
}

stop_stale_instance() {
  local pids pid remaining
  pids="$(known_pids | sort -u)"
  [ -n "${pids}" ] || return 0
  printf '%s\n' 'Phát hiện MariaDB cũ không healthy cho datadir này; chờ ngắn rồi dừng an toàn để tránh lock Aria/InnoDB.'
  for _ in $(seq 1 10); do db_ready && return 0; sleep 1; done
  while IFS= read -r pid; do kill -TERM "${pid}" 2>/dev/null || true; done <<< "${pids}"
  for _ in $(seq 1 15); do
    remaining="$(known_pids | sort -u)"
    [ -z "${remaining}" ] && return 0
    sleep 1
  done
  while IFS= read -r pid; do kill -KILL "${pid}" 2>/dev/null || true; done <<< "${remaining}"
  sleep 2
  if [ -n "$(known_pids | sort -u)" ]; then
    printf '%s\n' 'Không thể dừng tiến trình MariaDB cũ. Không xóa lock/socket và không khởi động instance thứ hai.' >&2
    exit 1
  fi
}

stop_stale_instance
if db_ready; then
  printf '%s\n' 'MariaDB đã sẵn sàng sau khi kiểm tra instance cũ.'
  exit 0
fi
rm -f "${PID_FILE}" "${SAFE_PID_FILE}" "${SOCKET}"

# Termux khuyến nghị mariadbd-safe thay vì --daemonize. Tiến trình safe
# sẽ giữ MariaDB sống và tự khởi động lại nếu daemon bị Android dừng.
mariadbd-safe \
  --defaults-file="${MYSQL_CNF}" \
  --datadir="${MYSQL_DATA}" \
  --socket="${SOCKET}" \
  --pid-file="${PID_FILE}" \
  --bind-address=127.0.0.1 \
  --port="${MYSQL_PORT}" \
  --skip-networking=0 \
  --skip-name-resolve \
  --feedback=OFF \
  --feedback-url= \
  --innodb-use-native-aio=0 \
  --log-error="${LOG_FILE}" \
  --skip-syslog \
  >/dev/null 2>&1 &
SAFE_PID=$!
printf '%s\n' "${SAFE_PID}" > "${SAFE_PID_FILE}"

for _ in $(seq 1 60); do
  if [ -S "${SOCKET}" ] && mariadb-admin --defaults-file="${MYSQL_CNF}" ping >/dev/null 2>&1; then
    printf '%s\n' "MariaDB đã sẵn sàng tại 127.0.0.1:${MYSQL_PORT} (socket: ${SOCKET})."
    exit 0
  fi
  if ! kill -0 "${SAFE_PID}" 2>/dev/null && [ ! -S "${SOCKET}" ]; then
    break
  fi
  sleep 1
done

printf '%s\n' "Không thể khởi động MariaDB bằng mariadbd-safe. 80 dòng log cuối:" >&2
tail -n 80 "${LOG_FILE}" >&2 || true
rm -f "${SAFE_PID_FILE}"
exit 1
