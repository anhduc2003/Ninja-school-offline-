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

Panel dùng session HttpOnly, SameSite=Strict, CSRF token, role-based access control và audit SQL append-only. Mọi write hiện có đều dùng parameterized query, allowlist cột/bảng và confirmation phrase. Các thay đổi item/shop/monster có thể cần reload cache hoặc restart Java server vì game giữ một phần dữ liệu trong bộ nhớ.

Không thay đổi `bindHost` thành `0.0.0.0` trừ khi bạn hiểu rủi ro mạng LAN và đã thay đổi mật khẩu admin mạnh. Không mở MariaDB `3306` ra Internet.
