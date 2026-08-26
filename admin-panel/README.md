# Ninja Control Room — Local-only, không đăng nhập

Ninja Control Room chạy cùng máy với MariaDB và Java game server. Từ bản local-only, panel **không có tài khoản, mật khẩu, cookie session hay RBAC**. Giao diện mở trực tiếp tại `http://127.0.0.1:18080` và backend luôn ép bind loopback `127.0.0.1`, kể cả khi `config.local.json` vô tình ghi địa chỉ khác.

> Bất kỳ ứng dụng nào có thể chạy trên cùng thiết bị đều có thể truy cập panel. Không mở port panel hoặc MariaDB ra LAN/Internet.

## Khởi động

Trên Termux/Linux, chạy:

```bash
bash admin-panel/start-panel.sh
```

Trên Windows, chạy:

```bat
admin-panel\run-panel-stack.cmd
```

Launcher dùng lock, kiểm tra dependency theo `package-lock.json`, dọn PID stale và chỉ báo sẵn sàng sau khi `GET http://127.0.0.1:18080/api/system/health` phản hồi. Nếu chưa có `admin-panel/config.local.json`, panel lấy `db.*` từ `config.properties` của game. File local này không bị Git sync hoặc installer ghi đè khi cập nhật source.

Mở `http://127.0.0.1:18080` ngay sau khi panel sẵn sàng. Không nhập tên tài khoản hoặc mật khẩu.

## Biện pháp an toàn còn giữ lại

Panel không dùng login nhưng vẫn tạo token CSRF chỉ tồn tại trong tiến trình hiện tại. Các request ghi dữ liệu cần header token này; trang web local tự lấy token khi tải. Mọi thao tác nhạy cảm vẫn cần confirmation phrase, dùng SQL parameterized/allowlist, tạo snapshot khi module hỗ trợ và ghi `panel_audit_events` với actor `local-only`.

Các thay đổi item, shop, monster và option có thể cần reload hoặc restart Java vì game giữ cache trong bộ nhớ. Chỉ số/hành trang nhân vật vẫn bị chặn khi người chơi online để tránh desync. Panel không tự dừng Java, không public MariaDB và không mô phỏng lệnh runtime không có contract SQL.

## Database với quyền tối thiểu

Lần đầu có thể dùng credential MariaDB local hiện có để tạo các bảng `panel_*` vận hành. Sau đó nên dùng user riêng; thay `nsoz` nếu database game dùng tên khác:

```sql
CREATE USER 'nso_panel'@'localhost' IDENTIFIED BY 'THAY_MAT_KHAU_DAI_NGHIEM_NGAT';
GRANT SELECT, INSERT, UPDATE, DELETE ON nsoz.* TO 'nso_panel'@'localhost';
FLUSH PRIVILEGES;
```

Sau bootstrap, cập nhật `admin-panel/config.local.json` với user riêng và đặt `"bootstrapSchema": false`. Khi database mới chưa có bảng `panel_*`, chạy một lần với user có quyền `CREATE`, rồi quay lại user ít quyền. Các bảng `panel_admin_users`, `panel_sessions` hoặc migration game-admin từ phiên bản cũ không còn được panel sử dụng; chúng có thể được giữ nguyên để bảo toàn dữ liệu lịch sử, không cần xóa để chạy local-only.

## Giới hạn mạng

Không đổi URL panel thành IP LAN và không reverse-proxy port `18080`. Health endpoint báo `access: "local-only-no-login"`; đây là dấu hiệu bản local-only đang chạy. Không commit `config.local.json`, `.termux/`, `data/`, `backups/`, `reports/` hoặc logs lên GitHub.
