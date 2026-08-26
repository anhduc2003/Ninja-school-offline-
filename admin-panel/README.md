# Ninja Control Room — Offline Panel

Panel chạy cùng máy với MariaDB và Java game server. Mặc định chỉ bind `127.0.0.1:18080`, không public MySQL và không gửi dữ liệu vận hành ra Internet.

## Khởi động

Trên Termux/Linux, sau khi MariaDB đang chạy:

```bash
bash admin-panel/start-panel.sh
```

Trên Windows:

```bat
admin-panel\run-panel-stack.cmd
```

Lệnh Windows cài dependency production nếu cần, khởi động panel và scheduler local. Truy cập bằng trình duyệt cùng máy tại `http://127.0.0.1:18080`. Lần chạy đầu tạo user `admin` và ghi mật khẩu tạm thời vào `admin-panel/data/first-login.txt` với quyền file hạn chế. Đổi mật khẩu trước khi dùng panel cho vận hành thực tế.

## An toàn vận hành

Panel dùng session HttpOnly, SameSite=Strict, CSRF token, role-based access control và audit SQL append-only. Mọi write hiện có đều dùng parameterized query, allowlist cột/bảng và confirmation phrase. Các thay đổi item/shop/monster có thể cần reload cache hoặc restart Java server vì game giữ một phần dữ liệu trong bộ nhớ. Mật khẩu được đổi trong mô-đun **Bảo mật tài khoản**; thao tác đó thu hồi toàn bộ session của user hiện tại.

Các view vận hành có dữ liệu thật gồm dashboard/health, người chơi, inventory JSON **chỉ đọc**, account status và ban, tiền tệ, item/shop/boss, gift code và lịch sử redemption, event points **chỉ đọc**, option rate/notify, leaderboard, analytics, incident/audit, backup, maintenance runbook và scheduler local. Vì lifecycle sự kiện, thông báo broadcast và nhiều cache nằm trong bộ nhớ Java, panel không giả vờ áp dụng các tác vụ đó live chỉ bằng SQL; các màn hình tương ứng nêu rõ khi cần restart/reload hoặc runbook thủ công.

## MariaDB local với quyền tối thiểu

Mẫu cấu hình mặc định tương thích máy game cũ (`root` không mật khẩu) để bootstrap lần đầu, nhưng **không nên dùng root lâu dài**. Sau khi panel đã tạo các bảng `panel_*` ở lần đầu khởi động, đăng nhập MariaDB local và tạo một user riêng. Thay `USE nsoz` nếu bạn đã đổi tên database game.

```sql
CREATE USER 'nso_panel'@'localhost' IDENTIFIED BY 'THAY_MAT_KHAU_DAI_NGHIEM_NGAT';
GRANT SELECT, INSERT, UPDATE, DELETE ON nsoz.* TO 'nso_panel'@'localhost';
FLUSH PRIVILEGES;
```

Sau đó sửa `admin-panel/config.local.json` với `user: "nso_panel"`, mật khẩu vừa tạo, và đặt `"bootstrapSchema": false`. Cờ này ngăn panel thử `CREATE TABLE` ở mỗi lần chạy, để tài khoản giới hạn không cần đặc quyền `CREATE`. Nếu di chuyển sang database mới chưa có bảng `panel_*`, chạy **một lần** với tài khoản quản trị local và `bootstrapSchema: true`, rồi quay về tài khoản `nso_panel`. Không commit `config.local.json`, `data/`, `backups/` hay `reports/`.

Không thay đổi `bindHost` thành `0.0.0.0` trừ khi bạn hiểu rủi ro mạng LAN và đã thay đổi mật khẩu admin mạnh. Không mở MariaDB `3306` ra Internet.
