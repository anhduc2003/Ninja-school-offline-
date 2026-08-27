# Reward Campaign Center

## Phạm vi

Reward Campaign Center quản lý bốn loại phần thưởng bằng cùng một mô hình chuẩn hóa: `fancung` tại NPC Hùng Vương, `newbie` tại NPC Admin, `topup` tại NPC Hùng Vương và `event` tại NPC do quản trị viên chọn. Giao diện sử dụng catalog item/option, preset điều kiện và form tiền thưởng; quản trị viên không phải nhập JSON.

## Mô hình dữ liệu

`panel_reward_campaigns` lưu metadata, server scope, điều kiện, thời gian hiệu lực, phạm vi claim và tiền thưởng Coin/Lượng/Yên. `panel_reward_items` lưu từng item reward. `panel_reward_item_options` lưu từng option với khoảng min/max để runtime có thể random giá trị. `panel_reward_claims` là lịch sử immutable và là lớp chống nhận trùng.

## Điều kiện runtime

Campaign Fancung kiểm tra `users.fancung`. Campaign newbie tái sử dụng `users.received_first_gift`. Campaign topup kiểm tra `users.tongnap`. Campaign event đọc `Char.eventPoint` theo key đã đăng ký trong Java event. `server_id = 0` áp dụng cho mọi server; giá trị cụ thể giới hạn campaign vào server đó. Claim scope có thể là mỗi tài khoản hoặc mỗi nhân vật.

## An toàn vận hành

Campaign đã có claim không được sửa hoặc xóa; chỉ có thể tắt. Các item và option đều phải tồn tại trong catalog game. Claim yêu cầu đủ slot túi, được ghi nhận và cập nhật các cờ tương thích hiện tại. Campaign active tương ứng sẽ thay thế menu hard-code cũ để tránh phát quà trùng.
