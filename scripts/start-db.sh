#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_DATA="${PREFIX}/var/lib/mysql"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
MYSQL_CNF="${ROOT_DIR}/.termux/mariadb.cnf"
PID_FILE="${ROOT_DIR}/.termux/mariadb.pid"
SAFE_PID_FILE="${ROOT_DIR}/.termux/mariadbd-safe.pid"
SOCKET="${MYSQL_RUN}/mysqld.sock"
LOG_FILE="${ROOT_DIR}/logs/mariadb.log"

mkdir -p "${MYSQL_RUN}" "${ROOT_DIR}/.termux" "${ROOT_DIR}/logs"

if [ ! -d "${MYSQL_DATA}/mysql" ]; then
  printf '%s\n' 'Chưa khởi tạo MariaDB. Hãy chạy lại install.sh hoặc scripts/setup-termux.sh.' >&2
  exit 1
fi

if [ ! -f "${MYSQL_CNF}" ]; then
  printf '%s\n' 'Thiếu cấu hình MariaDB. Hãy chạy lại scripts/setup-termux.sh.' >&2
  exit 1
fi

if [ -S "${SOCKET}" ] && mariadb-admin --defaults-file="${MYSQL_CNF}" ping >/dev/null 2>&1; then
  printf '%s\n' 'MariaDB đang chạy.'
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
  --port=3306 \
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
    printf '%s\n' "MariaDB đã sẵn sàng tại 127.0.0.1:3306 (socket: ${SOCKET})."
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
