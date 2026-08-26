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
USERNAME="${1:-}"

say() { printf '[Game admin] %s\n' "$*"; }
fail() { printf '[Game admin][ERROR] %s\n' "$*" >&2; exit 1; }
[[ "${USERNAME}" =~ ^[A-Za-z0-9_]{3,30}$ ]] || fail 'Dùng: ./scripts/grant-game-admin.sh <username> (3-30 ký tự: chữ, số, _).'

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
user_id="$(mysql -N -B -e "SELECT id FROM users WHERE username='${USERNAME}' LIMIT 1;")"
[ -n "${user_id}" ] || fail "Không tìm thấy account game: ${USERNAME}"

confirmation="GRANT GAME ADMIN ${USERNAME}"
if [ "${NSO_GAME_ADMIN_CONFIRM:-}" != "${confirmation}" ]; then
  printf 'Sẽ gán role Admin game (role_id=1) cho %s. Mật khẩu không bị thay đổi.\n' "${USERNAME}"
  read -r -p "Nhập chính xác '${confirmation}' để tiếp tục: " entered
  [ "${entered}" = "${confirmation}" ] || fail 'Đã hủy, không có thay đổi.'
fi

model_type="CONCAT('App',CHAR(92),'Modules',CHAR(92),'User',CHAR(92),'Models',CHAR(92),'User')"
existing="$(mysql -N -B -e "SELECT COUNT(*) FROM model_has_roles WHERE role_id=1 AND model_type=${model_type} AND model_id=${user_id};")"
if [ "${existing}" = "0" ]; then
  mysql -e "INSERT INTO model_has_roles (role_id, model_type, model_id) VALUES (1, ${model_type}, ${user_id});"
  say "Đã gán role Admin game cho ${USERNAME}."
  audit_table="$(mysql -N -B -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='panel_audit_events';")"
  if [ "${audit_table}" = "1" ]; then
    mysql -e "INSERT INTO panel_audit_events (id, actor_id, actor_username, module_name, action_name, resource_type, resource_id, outcome, correlation_id, metadata) VALUES (UUID(), NULL, 'local-cli', 'security', 'game_admin.role_granted', 'game_user', '${user_id}', 'success', UUID(), JSON_OBJECT('username','${USERNAME}','roleId',1));"
    say 'Đã ghi audit local (actor: local-cli).'
  fi
else
  say "${USERNAME} đã có role Admin game; không thay đổi dữ liệu."
fi

say 'Khởi động lại panel nếu nó đang dừng; session panel hiện có sẽ tái kiểm tra quyền game ở request tiếp theo.'
