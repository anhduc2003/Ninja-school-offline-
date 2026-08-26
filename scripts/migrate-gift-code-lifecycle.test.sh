#!/usr/bin/env bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "${WORK_DIR}"' EXIT
FIXTURE_ROOT="${WORK_DIR}/root"
FAKE_MYSQL="${WORK_DIR}/fake-mariadb"
STATE_DIR="${WORK_DIR}/state"
mkdir -p "${FIXTURE_ROOT}/logs" "${STATE_DIR}"
printf '%s\n' '[client]' > "${WORK_DIR}/mariadb.cnf"
printf '%s\n' 'db.dbname=fixture_nsoz' > "${FIXTURE_ROOT}/config.properties"

cat > "${FAKE_MYSQL}" <<'EOF'
#!/usr/bin/env bash
set -Eeuo pipefail
query=""
while [ "$#" -gt 0 ]; do
  if [ "$1" = "-e" ]; then query="$2"; break; fi
  shift
done
state="${FAKE_STATE_DIR:?}"
if [[ "$query" == *"information_schema.columns"* ]]; then
  column="$(sed -n "s/.*column_name='\([^']*\)'.*/\1/p" <<<"$query")"
  grep -qx "$column" "$state/columns" 2>/dev/null && printf '1\n' || printf '0\n'
elif [[ "$query" == *"information_schema.statistics"* ]]; then
  index="$(sed -n "s/.*index_name='\([^']*\)'.*/\1/p" <<<"$query")"
  grep -qx "$index" "$state/indexes" 2>/dev/null && printf '1\n' || printf '0\n'
elif [[ "$query" == *"ADD COLUMN"* ]]; then
  sed -n 's/.*ADD COLUMN \([a-z_]*\).*/\1/p' <<<"$query" >> "$state/columns"
  printf '%s\n' "$query" >> "$state/alters"
elif [[ "$query" == *"ADD INDEX"* ]]; then
  sed -n 's/.*ADD INDEX \([a-z_]*\).*/\1/p' <<<"$query" >> "$state/indexes"
  printf '%s\n' "$query" >> "$state/alters"
fi
EOF
chmod +x "${FAKE_MYSQL}"
touch "${STATE_DIR}/columns" "${STATE_DIR}/indexes" "${STATE_DIR}/alters"

run_migration() {
  ROOT_DIR="${FIXTURE_ROOT}" CNF_FILE="${WORK_DIR}/mariadb.cnf" MYSQL_BIN="${FAKE_MYSQL}" MYSQL_SOCKET="${WORK_DIR}/socket" FAKE_STATE_DIR="${STATE_DIR}" bash "${ROOT_DIR_SOURCE}/scripts/migrate-gift-code-lifecycle.sh"
}
ROOT_DIR_SOURCE="${ROOT_DIR}"
run_migration
test "$(sort "${STATE_DIR}/columns" | tr '\n' ' ')" = 'disabled max_redemptions redemption_count starts_at '
test "$(sort "${STATE_DIR}/indexes" | tr '\n' ' ')" = 'gift_codes_lifecycle_idx gift_codes_redemption_idx '
test "$(wc -l < "${STATE_DIR}/alters")" -eq 6
run_migration
test "$(wc -l < "${STATE_DIR}/alters")" -eq 6
grep -q 'fixture_nsoz' "${FIXTURE_ROOT}/logs/gift-code-migration.log" || true
grep -q 'Không import/reset dữ liệu cũ' "${FIXTURE_ROOT}/logs/gift-code-migration.log"
printf '%s\n' 'gift code lifecycle migration tests passed.'
