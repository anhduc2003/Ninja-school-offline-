# Offline Panel Verification Notes

## Local-only verification

Ninja Control Room được khởi động trong fixture MariaDB cục bộ với `config.local.json` cố tình đặt `bindHost: "0.0.0.0"`. Backend vẫn chỉ lắng nghe `127.0.0.1:18080`, và health endpoint trả `access: "local-only-no-login"`.

Trình duyệt fixture mở trực tiếp dashboard; không xuất hiện trường username/mật khẩu hoặc nút logout. Endpoint `/api/auth/login` trả HTTP `410`; `/api/modules` trả danh sách module mà không cần cookie. Response context local không đặt `Set-Cookie`.

POST thiếu `x-nso-csrf` bị trả HTTP `403`. POST có token từ `/api/local/context`, confirmation phrase hợp lệ và body hợp lệ đã tạo job draft thành công; audit ghi actor `local-only`. Điều này xác minh token CSRF theo tiến trình, confirmation phrase và audit tiếp tục hoạt động khi không còn login/session/RBAC.

## Automated checks

`npm run check`, `npm test`, `bash -n` cho launcher/scripts và Maven `-DskipTests package` được chạy trước phát hành. Unit suite hiện vẫn bao gồm các helper lịch sử cho password/RBAC/session nhằm bảo toàn compatibility library, nhưng backend local-only không import hoặc gọi chúng để cấp quyền panel.

## Platform note

Kiểm thử HTTP/visual chạy trong Linux sandbox. Không tuyên bố đã kiểm thử thiết bị Android thật. Launchers vẫn kiểm tra health local, giữ database/config/runtime người dùng qua Git sync, và panel không được mở qua LAN/Internet.
