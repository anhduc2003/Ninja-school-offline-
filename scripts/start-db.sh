#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_DATA="${PREFIX}/var/lib/mysql"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
PID_FILE="${ROOT_DIR}/.termux/mariadb.pid"
SOCKET="${MYSQL_RUN}/mysqld.sock"
LOG_FILE="${ROOT_DIR}/logs/mariadb.log"

mkdir -p "${MYSQL_RUN}" "${ROOT_DIR}/.termux" "${ROOT_DIR}/logs"

if [ ! -d "${MYSQL_DATA}/mysql" ]; then
  printf '%s\n' 'Chưa khởi tạo MariaDB. Hãy chạy scripts/setup-termux.sh trước.' >&2
  exit 1
fi

if [ -S "${SOCKET}" ] && mariadb-admin --socket="${SOCKET}" ping >/dev/null 2>&1; then
  printf '%s\n' 'MariaDB đang chạy.'
  exit 0
fi

rm -f "${PID_FILE}" "${SOCKET}"

mariadbd \
  --datadir="${MYSQL_DATA}" \
  --socket="${SOCKET}" \
  --pid-file="${PID_FILE}" \
  --bind-address=127.0.0.1 \
  --port=3306 \
  --skip-networking=0 \
  --log-error="${LOG_FILE}" \
  --daemonize

for _ in $(seq 1 30); do
  if mariadb-admin --socket="${SOCKET}" ping >/dev/null 2>&1; then
    printf '%s\n' "MariaDB đã sẵn sàng tại 127.0.0.1:3306 (socket: ${SOCKET})."
    exit 0
  fi
  sleep 1
done

printf '%s\n' "Không thể khởi động MariaDB. Xem ${LOG_FILE}." >&2
exit 1
