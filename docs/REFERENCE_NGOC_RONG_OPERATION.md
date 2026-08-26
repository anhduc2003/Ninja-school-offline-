# Ghi chú tham chiếu vận hành NgocRong-Termux

Nguồn tham chiếu do chủ repository cung cấp: `https://github.com/anhduc2003/NgocRong-Termux`. Repository là private, nên URL công khai trả 404 nhưng nội dung được kiểm tra bằng quyền GitHub đã kết nối ngày 2026-08-26.

## Mô hình vận hành quan sát được

NgocRong tách panel thành **Panel API** Node.js tại `127.0.0.1:3001` và **Panel Web** Vite tại `127.0.0.1:5173`. Script `termux/start-panel.sh` cài dependency bằng `npm ci`, tạo lock chống khởi động trùng, chạy đồng bộ database/config, ghi PID/log riêng, sau đó polling HTTP health tối đa 30 giây trước khi báo service sẵn sàng.

Script `panel/api/scripts/sync-database.js` lấy `Config.properties` của game làm nguồn cấu hình database/cổng, kiểm tra bảng game, áp dụng schema `panel_*`, upsert registry server và duy trì JWT secret qua các lần restart. API reference có health route `/api/v1/system/health`.

Mô hình reference còn dùng HTTP control agent nhúng JVM tại `127.0.0.1:9090` cho metric runtime, kick, broadcast, reload shop/giftcode/boss và maintenance. Đây là khác biệt quan trọng với kiến trúc Ninja SQL-only: mọi chức năng runtime như vậy chỉ có thể tương đương khi người dùng chấp nhận local JVM agent; không được tự ý thêm vào bản SQL-only.

## Áp dụng an toàn cho Ninja

Các phần có thể áp dụng mà không đổi kiến trúc SQL-only gồm: lấy `config.properties` làm nguồn cấu hình, health-check rõ ràng khi launcher chạy, lock/PID/log phân tách, dependency install xác định và thông báo URL/diagnostic nhất quán. Cần giữ panel Ninja bind `127.0.0.1` và không public MariaDB.
