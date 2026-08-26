#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
MYSQL_CNF="${ROOT_DIR}/.termux/mariadb.cnf"
SOCKET="${MYSQL_RUN}/mysqld.sock"
SAFE_PID_FILE="${ROOT_DIR}/.termux/mariadbd-safe.pid"

if [ -S "${SOCKET}" ] && [ -f "${MYSQL_CNF}" ]; then
  mariadb-admin --defaults-file="${MYSQL_CNF}" shutdown >/dev/null 2>&1 || true
fi

if [ -f "${SAFE_PID_FILE}" ]; then
  SAFE_PID="$(cat "${SAFE_PID_FILE}")"
  if kill -0 "${SAFE_PID}" 2>/dev/null; then
    kill "${SAFE_PID}" 2>/dev/null || true
  fi
  rm -f "${SAFE_PID_FILE}"
fi

rm -f "${SOCKET}" "${ROOT_DIR}/.termux/mariadb.pid"
printf '%s\n' 'Đã dừng MariaDB nếu tiến trình đang chạy.'
