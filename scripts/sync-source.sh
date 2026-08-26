#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="${ROOT_DIR}/logs"
LOG_FILE="${LOG_DIR}/sync.log"
REMOTE_NAME="${NSO_SYNC_REMOTE:-origin}"
BRANCH_NAME="${NSO_SYNC_BRANCH:-main}"
AUTO_SYNC="${NSO_AUTO_SYNC:-1}"
SERVER_PID_FILE="${ROOT_DIR}/.termux/server.pid"

mkdir -p "${LOG_DIR}" "${ROOT_DIR}/.termux"
log() { printf '%s %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*" | tee -a "${LOG_FILE}"; }

if [ "${AUTO_SYNC}" != "1" ]; then
  log '[SYNC] Bỏ qua: NSO_AUTO_SYNC khác 1.'
  exit 0
fi

if [ ! -d "${ROOT_DIR}/.git" ]; then
  log '[SYNC] Bỏ qua: bản cài hiện tại không có metadata Git. Cài/nâng lên Release mới để bật đồng bộ tự động.'
  exit 0
fi

if ! git -C "${ROOT_DIR}" rev-parse --verify HEAD >/dev/null 2>&1; then
  log '[SYNC] Bỏ qua: metadata Git chưa hoàn tất; giữ source Release hiện tại.'
  exit 0
fi

if [ -f "${SERVER_PID_FILE}" ] && kill -0 "$(cat "${SERVER_PID_FILE}")" 2>/dev/null; then
  log '[SYNC] Bỏ qua: game server đang chạy; không cập nhật source trong khi JVM còn hoạt động.'
  exit 0
fi
rm -f "${SERVER_PID_FILE}"

cd "${ROOT_DIR}"
if [ -n "$(git status --porcelain --untracked-files=normal)" ]; then
  log '[SYNC] Bỏ qua: phát hiện thay đổi source local chưa commit. Database/config/runtime local bị ignore và không bị coi là thay đổi source.'
  exit 0
fi

if ! git fetch --quiet "${REMOTE_NAME}" "${BRANCH_NAME}"; then
  log "[SYNC] Không kết nối được GitHub (${REMOTE_NAME}/${BRANCH_NAME}); tiếp tục chạy source local hiện tại."
  exit 0
fi

LOCAL_REV="$(git rev-parse HEAD)"
REMOTE_REV="$(git rev-parse "${REMOTE_NAME}/${BRANCH_NAME}")"
if [ "${LOCAL_REV}" = "${REMOTE_REV}" ]; then
  log "[SYNC] Đã mới nhất (${LOCAL_REV:0:12})."
  exit 0
fi

if ! git merge-base --is-ancestor "${LOCAL_REV}" "${REMOTE_REV}"; then
  log '[SYNC] Bỏ qua: source local diverged/ahead remote. Không reset hoặc ghi đè thay đổi local.'
  exit 0
fi

if git merge --ff-only --quiet "${REMOTE_REV}"; then
  rm -f "${ROOT_DIR}/target/Nso-jar-with-dependencies.jar" "${ROOT_DIR}/admin-panel/data/package-lock.sha256"
  log "[SYNC] Đã fast-forward ${LOCAL_REV:0:12} → ${REMOTE_REV:0:12}. JAR/dependency marker được làm mới trước khi start."
else
  log '[SYNC] Fast-forward thất bại; giữ source local hiện tại, không động vào database/config/runtime.'
fi
