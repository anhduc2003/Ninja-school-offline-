#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
MYSQL_CNF="${ROOT_DIR}/.termux/mariadb.cnf"
SOCKET="${MYSQL_RUN}/mysqld.sock"
SAFE_PID_FILE="${ROOT_DIR}/.termux/mariadbd-safe.pid"
PID_FILE="${ROOT_DIR}/.termux/mariadb.pid"

if [ -S "${SOCKET}" ] && [ -f "${MYSQL_CNF}" ]; then
  mariadb-admin --defaults-file="${MYSQL_CNF}" shutdown >/dev/null 2>&1 || true
fi

for PID_SOURCE in "${SAFE_PID_FILE}" "${PID_FILE}"; do
  if [ -f "${PID_SOURCE}" ]; then
    DB_PID="$(cat "${PID_SOURCE}" 2>/dev/null || true)"
    if [ -n "${DB_PID}" ] && kill -0 "${DB_PID}" 2>/dev/null; then kill -TERM "${DB_PID}" 2>/dev/null || true; fi
  fi
done
for _ in $(seq 1 15); do
  ACTIVE=0
  for PID_SOURCE in "${SAFE_PID_FILE}" "${PID_FILE}"; do
    [ -f "${PID_SOURCE}" ] && kill -0 "$(cat "${PID_SOURCE}")" 2>/dev/null && ACTIVE=1 || true
  done
  [ "${ACTIVE}" -eq 0 ] && break
  sleep 1
done
for PID_SOURCE in "${SAFE_PID_FILE}" "${PID_FILE}"; do
  if [ -f "${PID_SOURCE}" ] && kill -0 "$(cat "${PID_SOURCE}")" 2>/dev/null; then kill -KILL "$(cat "${PID_SOURCE}")" 2>/dev/null || true; fi
done
rm -f "${SAFE_PID_FILE}"

rm -f "${SOCKET}" "${ROOT_DIR}/.termux/mariadb.pid"
printf '%s\n' 'Đã dừng MariaDB nếu tiến trình đang chạy.'
