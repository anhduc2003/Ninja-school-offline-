#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DATA_DIR="${ROOT_DIR}/data"
PID_FILE="${DATA_DIR}/panel.pid"
LOG_FILE="${ROOT_DIR}/../logs/admin-panel.log"
LOCK_DIR="${DATA_DIR}/.panel-start.lock"
CONFIG_FILE="${ROOT_DIR}/config.local.json"
PANEL_PORT="${NSO_PANEL_PORT:-18080}"
if [ -f "${CONFIG_FILE}" ] && [ -z "${NSO_PANEL_PORT:-}" ]; then
  detected_port="$(sed -nE 's/^[[:space:]]*"port"[[:space:]]*:[[:space:]]*([0-9]+).*/\1/p' "${CONFIG_FILE}" | head -n1)"
  PANEL_PORT="${detected_port:-18080}"
fi
PANEL_URL="http://127.0.0.1:${PANEL_PORT}"
HEALTH_URL="${PANEL_URL}/api/system/health"

say() { printf '[Panel] %s\n' "$*"; }
fail() { printf '[Panel][ERROR] %s\n' "$*" >&2; exit 1; }
mkdir -p "${DATA_DIR}" "$(dirname "${LOG_FILE}")"
command -v node >/dev/null 2>&1 || { command -v pkg >/dev/null 2>&1 && pkg install -y nodejs-lts >/dev/null 2>&1 || fail 'Thiếu Node.js. Trên Termux hãy chạy: pkg install nodejs-lts'; }
command -v npm >/dev/null 2>&1 || fail 'Không tìm thấy npm sau khi kiểm tra Node.js.'
command -v curl >/dev/null 2>&1 || fail 'Thiếu curl; hãy cài bằng pkg install curl.'

if ! mkdir "${LOCK_DIR}" 2>/dev/null; then
  if [ -s "${LOCK_DIR}/pid" ] && kill -0 "$(cat "${LOCK_DIR}/pid" 2>/dev/null)" 2>/dev/null; then say 'Một tiến trình start-panel khác đang chạy; bỏ qua lần gọi trùng.'; exit 0; fi
  rm -rf "${LOCK_DIR}" && mkdir "${LOCK_DIR}"
fi
printf '%s\n' "$$" > "${LOCK_DIR}/pid"
trap 'rm -rf "${LOCK_DIR}"' EXIT

if [ -s "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}" 2>/dev/null)" 2>/dev/null; then say "Admin panel đang chạy với PID $(cat "${PID_FILE}"): ${PANEL_URL}"; exit 0; fi
rm -f "${PID_FILE}"
if curl -fsS --max-time 2 "${HEALTH_URL}" >/dev/null 2>&1; then say "Panel đã sẵn sàng tại ${PANEL_URL} (PID file cũ hoặc panel được khởi động ngoài launcher)."; exit 0; fi

DEPS_FINGERPRINT="$(sha256sum "${ROOT_DIR}/package-lock.json" | awk '{print $1}')"
DEPS_MARKER="${ROOT_DIR}/node_modules/.nso-deps-ready"
if [ ! -f "${DEPS_MARKER}" ] || [ "$(cat "${DEPS_MARKER}" 2>/dev/null || true)" != "${DEPS_FINGERPRINT}" ] || [ ! -s "${ROOT_DIR}/node_modules/mysql2/package.json" ] || [ ! -s "${ROOT_DIR}/node_modules/bcryptjs/index.js" ]; then
  say 'Cài/đồng bộ dependency panel theo package-lock...'
  (cd "${ROOT_DIR}" && npm ci --omit=dev --no-audit --no-fund)
  printf '%s\n' "${DEPS_FINGERPRINT}" > "${DEPS_MARKER}"
fi

say "Khởi động Ninja Control Room tại ${PANEL_URL}..."
nohup node "${ROOT_DIR}/server.mjs" >>"${LOG_FILE}" 2>&1 < /dev/null &
echo $! > "${PID_FILE}"
for _ in $(seq 1 30); do
  if ! kill -0 "$(cat "${PID_FILE}" 2>/dev/null)" 2>/dev/null; then break; fi
  if curl -fsS --max-time 2 "${HEALTH_URL}" >/dev/null 2>&1; then
    say "Panel đã sẵn sàng: ${PANEL_URL}"
    say "Health local: ${HEALTH_URL}"
    say "Log panel: ${LOG_FILE}"
    exit 0
  fi
  sleep 1
done
tail -80 "${LOG_FILE}" >&2 || true
rm -f "${PID_FILE}"
fail 'Panel không sẵn sàng trước thời hạn 30 giây.'
