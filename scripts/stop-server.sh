#!/data/data/com.termux/files/usr/bin/bash
set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_FILE="${ROOT_DIR}/.termux/server.pid"

if [ -f "${PID_FILE}" ]; then
  PID="$(cat "${PID_FILE}")"
  if kill -0 "${PID}" 2>/dev/null; then
    kill "${PID}"
    for _ in $(seq 1 15); do
      kill -0 "${PID}" 2>/dev/null || break
      sleep 1
    done
    if kill -0 "${PID}" 2>/dev/null; then
      kill -9 "${PID}" || true
    fi
    printf '%s\n' "Đã dừng server PID ${PID}."
  else
    printf '%s\n' 'PID cũ không còn tồn tại.'
  fi
  rm -f "${PID_FILE}"
else
  printf '%s\n' 'Không tìm thấy PID server.'
fi

if command -v termux-wake-unlock >/dev/null 2>&1; then
  termux-wake-unlock || true
fi
