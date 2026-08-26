#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="${NSO_SYNC_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)}"
LOG_DIR="${ROOT_DIR}/logs"
LOG_FILE="${LOG_DIR}/sync.log"
REMOTE_NAME="${NSO_SYNC_REMOTE:-origin}"
BRANCH_NAME="${NSO_SYNC_BRANCH:-main}"
REMOTE_URL="${NSO_SYNC_REPOSITORY:-https://github.com/anhduc2003/Ninja-school-offline-.git}"
AUTO_SYNC="${NSO_AUTO_SYNC:-1}"
BOOTSTRAP_SYNC="${NSO_SYNC_BOOTSTRAP:-1}"
SERVER_PID_FILE="${ROOT_DIR}/.termux/server.pid"
RELEASE_MARKER="${ROOT_DIR}/.termux/release-installed"

mkdir -p "${LOG_DIR}" "${ROOT_DIR}/.termux"
log() { printf '%s %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*" | tee -a "${LOG_FILE}"; }

if [ "${AUTO_SYNC}" != "1" ]; then
  log '[SYNC] Bỏ qua: NSO_AUTO_SYNC khác 1.'
  exit 0
fi

if ! command -v git >/dev/null 2>&1; then
  log '[SYNC] Bỏ qua: chưa có lệnh git. Chạy pkg install git, rồi khởi động lại server.'
  exit 0
fi

if [ -f "${SERVER_PID_FILE}" ] && kill -0 "$(cat "${SERVER_PID_FILE}")" 2>/dev/null; then
  log '[SYNC] Bỏ qua: game server đang chạy; không cập nhật source trong khi JVM còn hoạt động.'
  exit 0
fi
rm -f "${SERVER_PID_FILE}"

bootstrap_metadata() {
  if [ "${BOOTSTRAP_SYNC}" != "1" ]; then
    log '[SYNC] Bỏ qua: bản cài không có metadata Git và NSO_SYNC_BOOTSTRAP khác 1.'
    return 1
  fi
  if [ ! -f "${RELEASE_MARKER}" ]; then
    log '[SYNC] Bỏ qua: thiếu metadata Git và không nhận diện được bản cài Release an toàn để bootstrap. Cài/nâng lên Release mới hoặc clone Git repository.'
    return 1
  fi
  log "[SYNC] Không có .git; bootstrap metadata từ ${REMOTE_URL} cho bản cài Release. Config/database/log/runtime ignored sẽ được giữ nguyên."
  if ! git -C "${ROOT_DIR}" init -q; then
    log '[SYNC] Bootstrap thất bại: không thể khởi tạo metadata Git; giữ source Release hiện tại.'
    return 1
  fi
  if ! git -C "${ROOT_DIR}" remote add "${REMOTE_NAME}" "${REMOTE_URL}" 2>/dev/null; then
    log '[SYNC] Bootstrap thất bại: không thể cấu hình Git remote; giữ source Release hiện tại.'
    rm -rf "${ROOT_DIR}/.git"
    return 1
  fi
  if ! git -C "${ROOT_DIR}" fetch --prune "${REMOTE_NAME}" "${BRANCH_NAME}" >>"${LOG_FILE}" 2>&1; then
    log "[SYNC] Bootstrap thất bại: không fetch được ${REMOTE_NAME}/${BRANCH_NAME}; giữ source Release hiện tại."
    rm -rf "${ROOT_DIR}/.git"
    return 1
  fi
  RELEASE_VERSION="$(tr -d '\r\n' < "${RELEASE_MARKER}")"
  if [ -n "${RELEASE_VERSION}" ] && ! git -C "${ROOT_DIR}" fetch --quiet "${REMOTE_NAME}" "refs/tags/${RELEASE_VERSION}:refs/tags/${RELEASE_VERSION}" >>"${LOG_FILE}" 2>&1; then
    log "[SYNC] Bootstrap bỏ qua: không tải được tag Release ${RELEASE_VERSION}; không thể xác minh source trước khi cập nhật."
    rm -rf "${ROOT_DIR}/.git"
    return 1
  fi
  if [ -z "${RELEASE_VERSION}" ] || ! git -C "${ROOT_DIR}" rev-parse --verify --quiet "refs/tags/${RELEASE_VERSION}^{commit}" >/dev/null; then
    log "[SYNC] Bootstrap bỏ qua: release marker '${RELEASE_VERSION:-trống}' không có tag GitHub tương ứng; không thể xác minh source trước khi cập nhật."
    rm -rf "${ROOT_DIR}/.git"
    return 1
  fi
  if ! git -C "${ROOT_DIR}" read-tree "${RELEASE_VERSION}" || ! git -C "${ROOT_DIR}" update-index --refresh >/dev/null || ! git -C "${ROOT_DIR}" diff-index --quiet --cached "${RELEASE_VERSION}" -- || ! git -C "${ROOT_DIR}" diff-files --quiet || [ -n "$(git -C "${ROOT_DIR}" ls-files --others --exclude-standard)" ]; then
    log '[SYNC] Bootstrap bỏ qua: source hiện tại khác Release marker hoặc có file source local mới. Không reset/ghi đè source không xác minh được.'
    rm -rf "${ROOT_DIR}/.git"
    return 1
  fi
  if ! git -C "${ROOT_DIR}" reset --hard -q "${REMOTE_NAME}/${BRANCH_NAME}"; then
    log '[SYNC] Bootstrap thất bại: không thể nạp source remote; giữ source Release hiện tại.'
    rm -rf "${ROOT_DIR}/.git"
    return 1
  fi
  log "[SYNC] Bootstrap thành công tại $(git -C "${ROOT_DIR}" rev-parse --short HEAD). Source Release đã được nối với GitHub; lần start sau sẽ auto-update bằng fast-forward."
  return 0
}

if [ ! -d "${ROOT_DIR}/.git" ]; then
  bootstrap_metadata || exit 0
fi

if ! git -C "${ROOT_DIR}" rev-parse --verify HEAD >/dev/null 2>&1; then
  log '[SYNC] Bỏ qua: metadata Git chưa hoàn tất; giữ source Release hiện tại.'
  exit 0
fi

if ! git -C "${ROOT_DIR}" remote get-url "${REMOTE_NAME}" >/dev/null 2>&1; then
  if git -C "${ROOT_DIR}" remote add "${REMOTE_NAME}" "${REMOTE_URL}"; then
    log "[SYNC] Đã bổ sung remote ${REMOTE_NAME}: ${REMOTE_URL}"
  else
    log "[SYNC] Bỏ qua: không cấu hình được remote ${REMOTE_NAME}; giữ source local hiện tại."
    exit 0
  fi
fi

cd "${ROOT_DIR}"
if [ -n "$(git status --porcelain --untracked-files=normal)" ]; then
  log '[SYNC] Bỏ qua: phát hiện thay đổi source local chưa commit. Database/config/runtime local bị ignore và không bị coi là thay đổi source.'
  exit 0
fi

if ! git fetch --prune "${REMOTE_NAME}" "${BRANCH_NAME}" >>"${LOG_FILE}" 2>&1; then
  log "[SYNC] Không kết nối được GitHub (${REMOTE_NAME}/${BRANCH_NAME}); tiếp tục chạy source local hiện tại."
  exit 0
fi

LOCAL_REV="$(git rev-parse HEAD)"
REMOTE_REV="$(git rev-parse "${REMOTE_NAME}/${BRANCH_NAME}" 2>/dev/null || true)"
if [ -z "${REMOTE_REV}" ]; then
  log "[SYNC] Bỏ qua: không tìm thấy branch ${REMOTE_NAME}/${BRANCH_NAME} sau fetch."
  exit 0
fi
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
