#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

RELEASE_VERSION="${RELEASE_VERSION:-v1.0.1}"
RELEASE_REPO="${RELEASE_REPO:-anhduc2003/Ninja-school-offline-}"
RELEASE_ASSET="${RELEASE_ASSET:-ninja-school-termux-${RELEASE_VERSION}.tar.gz}"
INSTALL_DIR="${INSTALL_DIR:-$HOME/Ninja-school-offline-}"

printf '%s\n' '============================================='
printf '%s\n' ' Ninja School Offline - One Command Installer'
printf '%s\n' '============================================='

if [ "$(uname -o 2>/dev/null || true)" != "Android" ] && [ -z "${PREFIX:-}" ]; then
  printf '%s\n' 'Cảnh báo: script này được thiết kế cho Termux trên Android.' >&2
fi

printf '%s\n' '[1/6] Cài/cập nhật package Termux...'
pkg update -y
pkg upgrade -y
pkg install -y curl git mariadb
JAVA_MAJOR=""
if command -v java >/dev/null 2>&1; then
  JAVA_MAJOR="$(java -version 2>&1 | sed -nE 's/.*version "([0-9]+).*/\1/p' | head -n 1 || true)"
fi
if [ -z "${JAVA_MAJOR}" ] || [ "${JAVA_MAJOR}" -lt 17 ]; then
  if ! pkg install -y openjdk-21; then
    pkg install -y openjdk-17
  fi
fi
pkg install -y maven

printf '%s\n' '[2/6] Tải archive từ GitHub Release...'
ARCHIVE_URL="https://github.com/${RELEASE_REPO}/releases/download/${RELEASE_VERSION}/${RELEASE_ASSET}"
DOWNLOAD_DIR="$(mktemp -d)"
ARCHIVE_FILE="${DOWNLOAD_DIR}/${RELEASE_ASSET}"
trap 'rm -rf "${DOWNLOAD_DIR}"' EXIT

if [ -e "${INSTALL_DIR}" ]; then
  if [ ! -d "${INSTALL_DIR}/.git" ] && [ ! -f "${INSTALL_DIR}/.termux/release-installed" ]; then
    printf '%s\n' "Thư mục ${INSTALL_DIR} đã tồn tại nhưng không phải bản cài đặt của server." >&2
    printf '%s\n' 'Đặt INSTALL_DIR sang thư mục khác hoặc đổi tên thư mục hiện tại rồi chạy lại.' >&2
    exit 1
  fi
  if [ -x "${INSTALL_DIR}/scripts/stop-server.sh" ]; then
    bash "${INSTALL_DIR}/scripts/stop-server.sh" || true
  fi
  if [ -f "${INSTALL_DIR}/config.properties" ]; then
    cp "${INSTALL_DIR}/config.properties" "${DOWNLOAD_DIR}/config.properties.backup"
  fi
  rm -rf "${INSTALL_DIR}"
fi

curl -fL --retry 5 --retry-all-errors --progress-bar -o "${ARCHIVE_FILE}" "${ARCHIVE_URL}"
mkdir -p "${INSTALL_DIR}"
tar -xzf "${ARCHIVE_FILE}" -C "${INSTALL_DIR}"
if [ -f "${DOWNLOAD_DIR}/config.properties.backup" ]; then
  cp "${DOWNLOAD_DIR}/config.properties.backup" "${INSTALL_DIR}/config.properties"
fi
mkdir -p "${INSTALL_DIR}/.termux"
printf '%s\n' "${RELEASE_VERSION}" > "${INSTALL_DIR}/.termux/release-installed"

cd "${INSTALL_DIR}"
chmod +x scripts/*.sh
mkdir -p .termux logs

printf '%s\n' '[3/6] Tạo cấu hình server nếu chưa có...'
if [ ! -f config.properties ]; then
  cp config.properties.example config.properties
fi

printf '%s\n' '[4/6] Khởi tạo và import MariaDB...'
bash scripts/setup-termux.sh
bash scripts/init-db.sh

printf '%s\n' '[5/6] Build JAR server...'
mvn -DskipTests package

printf '%s\n' '[6/6] Khởi động game server headless...'
bash scripts/start-server.sh

printf '\n%s\n' '============================================='
printf '%s\n' 'CÀI ĐẶT VÀ KHỞI ĐỘNG HOÀN TẤT'
printf '%s\n' "Thư mục: ${INSTALL_DIR}"
printf '%s\n' 'Cổng game: 14444'
printf '%s\n' 'Xem log: tail -f logs/server.log'
printf '%s\n' 'Dừng server: bash scripts/stop-server.sh'
printf '%s\n' '============================================='
