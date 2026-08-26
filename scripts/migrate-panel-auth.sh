#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
MYSQL_RUN="${PREFIX}/var/run/mysqld"
SOCKET="${MYSQL_RUN}/mysqld.sock"
MYSQL_CNF="${ROOT_DIR}/.termux/mariadb.cnf"
CONFIG_FILE="${ROOT_DIR}/admin-panel/config.local.json"
GAME_CONFIG_FILE="${ROOT_DIR}/config.properties"
DB_USER="${DB_USER:-root}"

say() { printf '[Panel auth migration] %s\n' "$*"; }
fail() { printf '[Panel auth migration][ERROR] %s\n' "$*" >&2; exit 1; }

database_name() {
  local detected
  detected="$(sed -nE 's/^[[:space:]]*"name"[[:space:]]*:[[:space:]]*"([A-Za-z0-9_]+)".*/\1/p' "${CONFIG_FILE}" 2>/dev/null | head -n1 || true)"
  if [ -z "${detected}" ]; then
    detected="$(sed -nE 's/^[[:space:]]*db\.dbname[[:space:]]*=[[:space:]]*([A-Za-z0-9_]+).*/\1/p' "${GAME_CONFIG_FILE}" 2>/dev/null | head -n1 || true)"
  fi
  printf '%s' "${detected:-nsoz}"
}

DB_NAME="${DB_NAME:-$(database_name)}"
[[ "${DB_NAME}" =~ ^[A-Za-z0-9_]+$ ]] || fail 'Tên database không hợp lệ.'
command -v mariadb >/dev/null 2>&1 || fail 'Thiếu mariadb client. Trên Termux: pkg install mariadb'
[ -r "${MYSQL_CNF}" ] || fail "Không đọc được ${MYSQL_CNF}. Hãy chạy installer/khởi tạo MariaDB local trước."

if [ "${NSO_SKIP_DB_START:-0}" != "1" ]; then "${ROOT_DIR}/scripts/start-db.sh"; fi
[ -S "${SOCKET}" ] || fail "Không thấy socket MariaDB local: ${SOCKET}"

mysql() { mariadb --defaults-file="${MYSQL_CNF}" --socket="${SOCKET}" -u"${DB_USER}" "${DB_NAME}" "$@"; }
table_count="$(mysql -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='panel_admin_users';")"
[ "${table_count}" = "1" ] || fail 'Thiếu panel_admin_users. Đặt bootstrapSchema=true một lần để bootstrap panel trước, rồi chạy lại script này.'

has_column() { mysql -N -B -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='panel_admin_users' AND column_name='$1';"; }
if [ "$(has_column auth_source)" = "0" ]; then
  mysql -e "ALTER TABLE panel_admin_users ADD COLUMN auth_source ENUM('panel','game') NOT NULL DEFAULT 'panel' AFTER active;"
  say 'Đã thêm panel_admin_users.auth_source.'
fi
if [ "$(has_column game_user_id)" = "0" ]; then
  mysql -e "ALTER TABLE panel_admin_users ADD COLUMN game_user_id BIGINT NULL UNIQUE AFTER auth_source;"
  say 'Đã thêm panel_admin_users.game_user_id (UNIQUE).'
fi

say "Migration xác thực game-admin hoàn tất cho database ${DB_NAME}. Không đổi mật khẩu game hoặc panel cũ."
