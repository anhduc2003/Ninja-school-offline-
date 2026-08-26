#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PREFIX="${PREFIX:-/data/data/com.termux/files/usr}"
SOCKET="${MYSQL_SOCKET:-${PREFIX}/var/run/mysqld/mysqld.sock}"
DB_NAME="${DB_NAME:-nsoz}"
DB_USER="${DB_USER:-root}"
CNF_FILE="${ROOT_DIR}/.termux/mariadb.cnf"
MYSQL_BIN="${MYSQL_BIN:-mariadb}"

if [[ ! "${DB_NAME}" =~ ^[A-Za-z0-9_]+$ ]]; then
  printf '%s\n' 'DB_NAME chỉ được chứa chữ cái, số và dấu gạch dưới.' >&2
  exit 1
fi
if [ ! -f "${CNF_FILE}" ]; then
  printf '%s\n' "Thiếu ${CNF_FILE}. Hãy chạy bash scripts/setup-termux.sh trước." >&2
  exit 1
fi

MYSQL=("${MYSQL_BIN}" --defaults-file="${CNF_FILE}" --socket="${SOCKET}" -u"${DB_USER}" "${DB_NAME}")
if ! "${MYSQL[@]}" -e "SELECT 1;" >/dev/null 2>&1; then
  bash "${ROOT_DIR}/scripts/start-db.sh"
fi
has_column() {
  "${MYSQL[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='gift_codes' AND column_name='$1';" | grep -qx '1'
}
has_index() {
  "${MYSQL[@]}" -N -B -e "SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='gift_codes' AND index_name='$1';" | grep -qx '1'
}

"${MYSQL[@]}" -e "SELECT 1 FROM gift_codes LIMIT 1;" >/dev/null
if ! has_column starts_at; then "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN starts_at TIMESTAMP NULL DEFAULT NULL AFTER expires_at;"; fi
if ! has_column max_redemptions; then "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN max_redemptions INT UNSIGNED NULL DEFAULT NULL AFTER starts_at;"; fi
if ! has_column redemption_count; then "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN redemption_count INT UNSIGNED NOT NULL DEFAULT 0 AFTER max_redemptions;"; fi
if ! has_column disabled; then "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD COLUMN disabled TINYINT(1) NOT NULL DEFAULT 0 AFTER redemption_count;"; fi
if ! has_index gift_codes_lifecycle_idx; then "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD INDEX gift_codes_lifecycle_idx (code, server_id, disabled, starts_at, expires_at);"; fi
if ! has_index gift_codes_redemption_idx; then "${MYSQL[@]}" -e "ALTER TABLE gift_codes ADD INDEX gift_codes_redemption_idx (disabled, redemption_count);"; fi

printf '%s\n' 'Migration Gift Code lifecycle hoàn tất: starts_at, max_redemptions, redemption_count, disabled và index đã sẵn sàng.'
