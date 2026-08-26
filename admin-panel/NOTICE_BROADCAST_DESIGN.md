# Thiết kế thông báo toàn server

## Hiện trạng

- Module `notices` của panel hiện chỉ đọc/sửa option `notify` qua `/api/options` và `/api/actions/option-update`.
- Java đã có `GlobalService.getInstance().chat(name, text)` để gửi lệnh `CMD.CHAT_SERVER` tới toàn bộ `ServerManager.getChars()` đang online.
- Panel local-only chạy trên `127.0.0.1:18080`, có CSRF, confirmation phrase và audit nhưng chưa có runtime bridge.

## Thiết kế triển khai

1. Java mở một HTTP control endpoint chỉ bind `127.0.0.1`, cổng cấu hình riêng (`server.control.port`, mặc định `18081`) và yêu cầu bearer token (`server.control.token`).
2. Endpoint chỉ cho `POST /api/control/broadcast`, giới hạn body, kiểm tra token, kiểm tra `sender` và `message`, gọi `GlobalService.getInstance().chat(sender, message)`, trả số người online.
3. Panel thêm cấu hình runtime control lấy từ `config.properties` khi tạo `config.local.json`; endpoint panel `/api/actions/notice-broadcast` kiểm tra confirmation phrase, gọi bridge, ghi audit, không coi việc sửa `options.notify` là broadcast runtime.
4. UI module Thông báo có form sender/message, hiển thị trạng thái kết nối và lịch sử audit gần đây. Mọi thao tác broadcast cần xác nhận lại bằng phrase chứa nội dung gửi.
5. Cập nhật config mẫu, README/runbook, test endpoint/payload validation và chạy `mvn test`/`npm test`/`npm run check` tương ứng.

## Giới hạn

- Broadcast chỉ tới nhân vật đang online trong instance Java hiện tại; người chơi offline không nhận lại thông báo.
- Không mở endpoint ra LAN/Internet; token không commit vào source.
- `options.notify` tiếp tục là cấu hình persisted theo Java contract, còn broadcast là push runtime riêng.
