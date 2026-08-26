# Offline Panel Verification Notes

## Local-only verification

Ninja Control Room được khởi động trong fixture MariaDB cục bộ với `config.local.json` cố tình đặt `bindHost: "0.0.0.0"`. Backend vẫn chỉ lắng nghe `127.0.0.1:18080`, và health endpoint trả `access: "local-only-no-login"`.

Trình duyệt fixture mở trực tiếp dashboard; không xuất hiện trường username/mật khẩu hoặc nút logout. Endpoint `/api/auth/login` trả HTTP `410`; `/api/modules` trả danh sách module mà không cần cookie. Response context local không đặt `Set-Cookie`.

POST thiếu `x-nso-csrf` bị trả HTTP `403`. POST có token từ `/api/local/context`, confirmation phrase hợp lệ và body hợp lệ đã tạo job draft thành công; audit ghi actor `local-only`. Điều này xác minh token CSRF theo tiến trình, confirmation phrase và audit tiếp tục hoạt động khi không còn login/session/RBAC.

## Automated checks

`npm run check`, `npm test`, `bash -n` cho launcher/scripts và Maven `-DskipTests package` được chạy trước phát hành. Unit suite hiện vẫn bao gồm các helper lịch sử cho password/RBAC/session nhằm bảo toàn compatibility library, nhưng backend local-only không import hoặc gọi chúng để cấp quyền panel.

## Event Control pending apply

`event-control.test.mjs` xác minh catalog có `OFF`/`StarFestival`, validator drop JSON và cập nhật đúng key `game.event`/`event.*` mà không làm mất property khác. E2E MariaDB/HTTP fixture đã kiểm tra catalog, `event_points`, CSRF local-only, confirmation `QUEUE EVENT STAR_FESTIVAL`, audit `event.plan.queued`, chặn item drop ID không tồn tại, hủy plan có confirmation/audit, và `apply-event-plan.mjs` sao lưu config, ghi override drop, lưu history `applied` rồi xóa pending plan. `mvn -DskipTests package` pass sau khi thêm Star Festival, event ID 9 và helper override cho Noel/Tết/Mùa hè/Trung Thu.

## Gift Code lifecycle

Fixture bắt đầu từ schema `gift_codes` cũ, sau đó `migrate-gift-code-lifecycle.sh` được kiểm tra thêm đúng `starts_at`, `max_redemptions`, `redemption_count`, `disabled` và hai index, không xóa row hiện có. E2E HTTP xác minh catalog lifecycle/options, CSRF local-only, confirmation tạo/sửa/tắt, reward nhiều item/options/hạn, chặn item ID sai, state scheduled/disabled, audit `gift_code.created/updated/disabled`, và history redemption. Code có `redemption_count > 0` bị từ chối sửa/xóa nhưng vẫn có thể disable để bảo toàn đối soát. `gift-code-control.test.mjs` kiểm tra contract SQL/Java/API; Maven build pass với query `FOR UPDATE`, transaction Hikari, quota atomic và `expire_days` được tính lúc redemption.

## Platform note

Kiểm thử HTTP/visual chạy trong Linux sandbox. Không tuyên bố đã kiểm thử thiết bị Android thật. Launchers vẫn kiểm tra health local, giữ database/config/runtime người dùng qua Git sync, và panel không được mở qua LAN/Internet.
