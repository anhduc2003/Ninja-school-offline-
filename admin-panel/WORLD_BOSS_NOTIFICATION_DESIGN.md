# World Boss Notification Center

## Phạm vi

Mô-đun quản lý thông báo Boss thế giới cấu hình hai thời điểm: Boss xuất hiện (`spawn`) và Boss bị hạ (`defeat`). Runtime Java đọc cấu hình từ `panel_world_boss_notifications` và gửi qua `GlobalService.chat`, vì vậy người chơi online nhận cùng kênh chat server hiện có.

## Phạm vi server và group

`server_id = 0` là cấu hình global. Nếu cùng group/event có cấu hình cụ thể cho server hiện tại, cấu hình cụ thể được ưu tiên. Các group bám theo `SpawnBossManager`: `normal`, `vdmq`, `ltt`, `lc`, `hangvithu` và `SK`.

## Template

Các placeholder hợp lệ là `{boss}`, `{map}`, `{zone}`, `{killer}` và `{time}`. Placeholder được thay thế trong Java runtime; template lạ bị panel từ chối trước khi lưu. Nút Test gửi nội dung mẫu qua runtime bridge nhưng không tạo Boss giả, không tính kill và không ghi log gameplay.

## Chống spam và fallback

Mỗi cấu hình có `cooldown_seconds`, được giữ trong tiến trình Java. Khi database chưa có bảng/cấu hình hoặc truy vấn lỗi, thông báo spawn mặc định của server được giữ lại. Thông báo defeat chỉ áp dụng cho mob instance do `SpawnBossManager` tạo, không ảnh hưởng boss dungeon, clan hoặc boss thông thường.

## Audit và lịch sử

Panel ghi audit cho save, enable/disable và test. Runtime ghi `panel_world_boss_notification_logs` cho các lần phát tự động, gồm loại sự kiện, tên Boss, map, khu, số người chơi online và nội dung thực tế.
