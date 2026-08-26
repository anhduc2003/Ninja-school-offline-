#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
SOCKET="${MYSQL_RUN}/mysqld.sock"
DB_NAME="${DB_NAME:-nsoz}"
DB_USER="${DB_USER:-root}"

if [[ ! "${DB_NAME}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '%s\n' "Tên database không hợp lệ: ${DB_NAME}. Chỉ dùng chữ cái, số và dấu gạch dưới." >&2
  exit 1
fi

"${ROOT_DIR}/scripts/start-db.sh"

CREATE_DATABASE_SQL="CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mariadb --defaults-file="${ROOT_DIR}/.termux/mariadb.cnf" --socket="${SOCKET}" -u"${DB_USER}" -e "${CREATE_DATABASE_SQL}"

TABLE_COUNT="$(mariadb --defaults-file="${ROOT_DIR}/.termux/mariadb.cnf" --socket="${SOCKET}" -N -B -u"${DB_USER}" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';")"
if [ "${TABLE_COUNT}" -eq 0 ]; then
  printf '%s\n' "Đang import SQL vào database ${DB_NAME}..."
  mariadb --defaults-file="${ROOT_DIR}/.termux/mariadb.cnf" --socket="${SOCKET}" -u"${DB_USER}" "${DB_NAME}" < "${ROOT_DIR}/SQL/nsoz.sql"
  printf '%s\n' 'Import SQL hoàn tất.'
else
  printf '%s\n' "Database ${DB_NAME} đã có ${TABLE_COUNT} bảng; bỏ qua import để tránh ghi đè dữ liệu."
fi
