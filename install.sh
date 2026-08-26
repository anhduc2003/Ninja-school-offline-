#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

REPO_URL="${REPO_URL:-https://github.com/anhduc2003/Ninja-school-offline-.git}"
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
pkg install -y git mariadb
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

printf '%s\n' '[2/6] Clone hoặc cập nhật mã nguồn...'
if [ -d "${INSTALL_DIR}/.git" ]; then
  git -C "${INSTALL_DIR}" pull --ff-only
else
  if [ -e "${INSTALL_DIR}" ]; then
    printf '%s\n' "Thư mục ${INSTALL_DIR} đã tồn tại nhưng không phải Git repository." >&2
    printf '%s\n' 'Đặt INSTALL_DIR sang thư mục khác hoặc đổi tên thư mục hiện tại rồi chạy lại.' >&2
    exit 1
  fi
  git clone "${REPO_URL}" "${INSTALL_DIR}"
fi

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
