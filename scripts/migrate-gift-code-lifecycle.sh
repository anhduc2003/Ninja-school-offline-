#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="${ROOT_DIR:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
SOCKET="${MYSQL_SOCKET:-${PREFIX}/var/run/mysqld/mysqld.sock}"
GAME_CONFIG_PATH="${GAME_CONFIG_PATH:-${ROOT_DIR}/config.properties}"
config_value() { [ -f "${GAME_CONFIG_PATH}" ] && awk -F= -v key="$1" '$1 == key { value=substr($0, index($0, "=") + 1) } END { print value }' "${GAME_CONFIG_PATH}"; }
DB_NAME="${DB_NAME:-$(config_value db.dbname)}"
DB_NAME="${DB_NAME:-nsoz}"
DB_USER="${DB_USER:-root}"
CNF_FILE="${CNF_FILE:-${ROOT_DIR}/.termux/mariadb.cnf}"
MYSQL_BIN="${MYSQL_BIN:-mariadb}"
MIGRATION_LOG="${MIGRATION_LOG:-${ROOT_DIR}/logs/gift-code-migration.log}"

log() { mkdir -p "$(dirname "${MIGRATION_LOG}")"; printf '%s [GIFT-MIGRATION] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*" | tee -a "${MIGRATION_LOG}"; }

if [[ ! "${DB_NAME}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '%s\n' 'DB_NAME chỉ được chứa chữ cái, số và dấu gạch dưới.' >&2
  exit 1
fi
if [ ! -f "${CNF_FILE}" ]; then
  log "Thiếu ${CNF_FILE}. Hãy chạy bash scripts/setup-termux.sh trước."
  exit 1
fi

MYSQL=("${MYSQL_BIN}" --defaults-file="${CNF_FILE}" --socket="${SOCKET}" -u"${DB_USER}" "${DB_NAME}")
if ! "${MYSQL[@]}" -e "SELECT 1;" >/dev/null 2>&1; then
  log "MariaDB chưa phản hồi, thử khởi động local database trước migration."
  bash "${ROOT_DIR}/scripts/start-db.sh"
fi
if ! "${MYSQL[@]}" -e "SELECT 1;" >/dev/null 2>&1; then log "Không thể kết nối MariaDB để kiểm tra Gift Code lifecycle."; exit 1; fi
has_column() {
  "${MYSQL[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gift_codes' AND column_name='$1';" | grep -qx '1'
}
has_index() {
  "${MYSQL[@]}" -N -B -e "SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='gift_codes' AND index_name='$1' LIMIT 1;" | grep -qx '1'
}

"${MYSQL[@]}" -e "SELECT 1 FROM gift_codes LIMIT 1;" >/dev/null
if ! has_column starts_at; then log "Thêm cột starts_at."; "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN starts_at TIMESTAMP NULL DEFAULT NULL AFTER expires_at;"; fi
if ! has_column max_redemptions; then log "Thêm cột max_redemptions."; "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN max_redemptions INT UNSIGNED NULL DEFAULT NULL AFTER starts_at;"; fi
if ! has_column redemption_count; then log "Thêm cột redemption_count."; "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN redemption_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER max_redemptions;"; fi
if ! has_column disabled; then log "Thêm cột disabled."; "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN disabled TINYINT(1) NOT NULL DEFAULT 0 AFTER redemption_count;"; fi
if ! has_index gift_codes_lifecycle_idx; then log "Thêm index gift_codes_lifecycle_idx."; "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD INDEX gift_codes_lifecycle_idx (code, server_id, disabled, starts_at, expires_at);"; fi
if ! has_index gift_codes_redemption_idx; then log "Thêm index gift_codes_redemption_idx."; "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD INDEX gift_codes_redemption_idx (disabled, redemption_count);"; fi

log 'Migration Gift Code lifecycle sẵn sàng: starts_at, max_redemptions, redemption_count, disabled và index. Không import/reset dữ liệu cũ.'
