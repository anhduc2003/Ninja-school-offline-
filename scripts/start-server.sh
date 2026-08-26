#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="${ROOT_DIR}/.termux/server.pid"
LOG_FILE="${ROOT_DIR}/logs/server.log"
JAR_FILE="${ROOT_DIR}/target/Nso-jar-with-dependencies.jar"

mkdir -p "${ROOT_DIR}/.termux" "${ROOT_DIR}/logs"

if [ ! -f "${ROOT_DIR}/config.properties" ]; then
  cp "${ROOT_DIR}/config.properties.example" "${ROOT_DIR}/config.properties"
  printf '%s\n' 'Đã tạo config.properties. Hãy kiểm tra tài khoản/database trước khi chạy lại.'
fi

if [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  printf '%s\n' "Server đang chạy với PID $(cat "${PID_FILE}")."
  exit 0
fi

if [ ! -f "${JAR_FILE}" ]; then
  printf '%s\n' 'Chưa có JAR build; đang chạy Maven package...'
  mvn -DskipTests package
fi

if [ ! -d "${ROOT_DIR}/Data" ]; then
  printf '%s\n' 'Thiếu thư mục Data/. Hãy clone đầy đủ repository trước khi chạy.' >&2
  exit 1
fi

if command -v termux-wake-lock >/dev/null 2>&1; then
  termux-wake-lock || true
fi

JAVA_OPTS="${JAVA_OPTS:--Xms256m -Xmx1024m}"
printf '%s\n' "Khởi động server với JAVA_OPTS=${JAVA_OPTS}"
cd "${ROOT_DIR}"
nohup java ${JAVA_OPTS} -Dfile.encoding=UTF-8 -Dninja.headless=true \
  -jar "${JAR_FILE}" >>"${LOG_FILE}" 2>&1 < /dev/null &
echo $! > "${PID_FILE}"
sleep 3

if kill -0 "$(cat "${PID_FILE}")" 2>/dev/null; then
  printf '%s\n' "Server đã chạy với PID $(cat "${PID_FILE}"). Log: ${LOG_FILE}"
else
  printf '%s\n' "Server không khởi động được; xem log: ${LOG_FILE}" >&2
  rm -f "${PID_FILE}"
  exit 1
fi
