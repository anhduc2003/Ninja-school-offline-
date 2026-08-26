# Windows Offline Stack Runbook

Trước khi khởi động, xác nhận `java -version`, `mvn -version`, `node --version` và `sc query MariaDB` hoạt động trong Command Prompt. Nếu service MariaDB có tên khác, đặt `NSO_MYSQL_SERVICE` cho đúng tên trước khi chạy, ví dụ `set NSO_MYSQL_SERVICE=MySQL80`.

| Bước | Lệnh | Kết quả cần thấy |
|---|---|---|
| Khởi động stack | `scripts\windows-start-stack.cmd` | Game TCP tại `127.0.0.1:14444` và panel tại `http://127.0.0.1:18080` |
| Kiểm tra TCP game | `netstat -ano | findstr :14444` | Có socket `LISTENING` sau khi Java game server sẵn sàng |
| Kiểm tra panel | `netstat -ano | findstr :18080` | Có listener local Node.js |
| Đăng nhập lần đầu | Mở `admin-panel\data\first-login.txt` | Đăng nhập được bằng user local `admin`, sau đó đổi mật khẩu |
| Kiểm tra scheduler | Xem `logs\admin-scheduler.log` | Có dòng khởi động scheduler; job chỉ chạy sau approval trong panel |
| Dừng stack | `scripts\windows-stop-stack.cmd` | Java/panel/scheduler dừng; script hỏi riêng trước khi dừng MariaDB service |

Khi kiểm thử lần đầu, hãy chạy một job `health_check` ở trạng thái draft rồi phê duyệt qua panel. Không bật tự động `event_transition` hoặc `maintenance_transition` cho đến khi có runbook gameplay riêng.
