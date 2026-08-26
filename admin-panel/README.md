# Ninja Control Room — Offline Panel

Panel chạy cùng máy với MariaDB và Java game server. Mặc định chỉ bind `127.0.0.1:18080`, không public MySQL và không gửi dữ liệu vận hành ra Internet.

## Khởi động

Trên Termux/Linux, sau khi MariaDB đang chạy:

```bash
bash admin-panel/start-panel.sh
```

Launcher dùng lock chống gọi trùng, cài dependency đúng theo `package-lock.json`, dọn PID stale, rồi chỉ báo thành công sau khi endpoint local `http://127.0.0.1:18080/api/system/health` phản hồi. Lần đầu, nếu chưa có `admin-panel/config.local.json`, panel lấy các khóa `db.*` từ `config.properties` của game để tạo cấu hình local. Các lần sau không tự ghi đè file local đó. Khi database chưa có admin panel, panel tạo user **`admin`** với mật khẩu bootstrap **`1`**, đồng thời ghi lại ở `admin-panel/data/first-login.txt`.

Trên Windows:

```bat
admin-panel\run-panel-stack.cmd
```

Lệnh Windows cài dependency production nếu cần, kiểm tra health endpoint rồi khởi động panel và scheduler local. Truy cập bằng trình duyệt cùng máy tại `http://127.0.0.1:18080`; health local là `http://127.0.0.1:18080/api/system/health`. Lần chạy đầu tạo user `admin` với mật khẩu bootstrap **`1`** và ghi lại tại `admin-panel/data/first-login.txt` với quyền file hạn chế. Đây là mật khẩu rất yếu: đăng nhập xong, vào **Bảo mật tài khoản** để đổi mật khẩu ngay trước khi mở panel cho bất kỳ mạng LAN nào.

Mật khẩu bootstrap chỉ áp dụng khi bảng `panel_admin_users` **chưa có admin**; bản nâng cấp không tự ghi đè mật khẩu admin đang tồn tại. Nếu cần đặt một mật khẩu khác lúc khởi tạo, khởi động lần đầu với biến môi trường `NSO_PANEL_ADMIN_PASSWORD`. Không xóa bảng admin chỉ để trở về mật khẩu `1` trên server đang vận hành.

## An toàn vận hành

Panel dùng session HttpOnly, SameSite=Strict, CSRF token, role-based access control và audit SQL append-only. Mọi write hiện có đều dùng parameterized query, allowlist cột/bảng và confirmation phrase. Các thay đổi item/shop/monster có thể cần reload cache hoặc restart Java server vì game giữ một phần dữ liệu trong bộ nhớ. Mật khẩu được đổi trong mô-đun **Bảo mật tài khoản**; thao tác đó thu hồi toàn bộ session của user hiện tại.

Các view vận hành có dữ liệu thật gồm dashboard/health, người chơi, inventory JSON `bag/box/equiped/fashion` có chỉnh sửa offline với snapshot/audit, account status và ban, tiền tệ, item/shop/boss, gift code và lịch sử redemption, event points **chỉ đọc**, option rate/notify, leaderboard, analytics, incident/audit, backup, maintenance runbook và scheduler local. Vì lifecycle sự kiện, thông báo broadcast và nhiều cache nằm trong bộ nhớ Java, panel không giả vờ áp dụng các tác vụ đó live chỉ bằng SQL; các màn hình tương ứng nêu rõ khi cần restart/reload hoặc runbook thủ công.

## Tạo account, vật phẩm và shop NPC

Mô-đun **Tài khoản** tạo user game trong bảng `users` với bcrypt `$2y$`, tương thích helper `StringUtils.checkPassword` của Java Ninja. Username chỉ nhận 3–30 ký tự chữ, số hoặc `_`; mật khẩu game phải dài 8–100 ký tự. Mật khẩu không xuất hiện trong audit hay response UI. Việc tạo account có confirmation phrase và chỉ role `moderator` trở lên được thực hiện.

Mô-đun **Vật phẩm tùy biến** tạo/chỉnh catalog `item` với các field có trong schema: name, type, gender, description, level, icon, part, fashion và `isUpToUp`. Mô-đun **Cửa hàng** vẫn quản lý `shopcoin_tb1`, đồng thời quản lý catalog `stores` và hàng `store_data` cho shop NPC, gồm item template, sys, trạng thái khóa, giá coin/lượng/yên, expire và options JSON array. Tất cả create/update/delete đều có RBAC, CSRF, confirmation phrase và audit append-only.

> `StoreManager` và item catalog được Java tải vào memory. Sau khi đổi item, `stores` hoặc `store_data`, hãy thực hiện reload/restart Java theo runbook đã duyệt trước khi coi thay đổi đã áp dụng cho người chơi online. Panel SQL-only không tự chạy lệnh reload runtime.

## Chỉnh chỉ số nhân vật và hành trang

Mô-đun **Người chơi** mở workflow chỉnh trực tiếp cho `point`, `spoint`, `potential`, EXP trong `players.data.exp`, số ô túi/rương và bốn JSON hành trang `bag`, `box`, `equiped`, `fashion`. Java tự suy level từ EXP; panel không cho nhập level giả. Mọi write bị từ chối nếu `players.online = 1`, chạy trong SQL transaction, cần confirmation phrase và tạo bản chụp trước/sau ở `panel_player_snapshots` kèm audit. Người chơi phải thoát game hoàn toàn rồi đăng nhập lại để Java nạp state mới.

Hành trang chỉ chấp nhận JSON array theo Item/Equip contract của Java, giới hạn số slot, cấm field lạ/index trùng, kiểm tra template item mới có trong catalog và kiểm tra equipment type cho `equiped`/`fashion`. `mount`, `bijuu`, `effect`, `mask_box` và `collection_box` chưa được mở ghi vì có contract chuyên biệt hoặc state runtime bổ sung.

Khi nâng từ bản cũ và dùng `bootstrapSchema: false`, hãy tạm thời chạy panel một lần với tài khoản MariaDB có quyền `CREATE` và `bootstrapSchema: true` để tạo bảng `panel_player_snapshots`; sau đó trả lại tài khoản ít quyền và đặt `bootstrapSchema: false`. Không xóa snapshot trước khi đã đối soát audit hoặc hoàn tất quy trình phục hồi nội bộ.

Mô-đun **Vật phẩm tùy biến** hỗ trợ tìm theo ID/tên, lọc theo `type` thực có trong catalog cùng tùy chọn `gender`; mỗi lựa chọn type hiển thị số template tương ứng. Filter chỉ đọc, còn create/update item vẫn có confirmation và audit riêng.

## MariaDB local với quyền tối thiểu

Mẫu cấu hình mặc định tương thích máy game cũ (`root` không mật khẩu) để bootstrap lần đầu, nhưng **không nên dùng root lâu dài**. Sau khi panel đã tạo các bảng `panel_*` ở lần đầu khởi động, đăng nhập MariaDB local và tạo một user riêng. Thay `USE nsoz` nếu bạn đã đổi tên database game.

```sql
CREATE USER 'nso_panel'@'localhost' IDENTIFIED BY 'THAY_MAT_KHAU_DAI_NGHIEM_NGAT';
GRANT SELECT, INSERT, UPDATE, DELETE ON nsoz.* TO 'nso_panel'@'localhost';
FLUSH PRIVILEGES;
```

Sau đó sửa `admin-panel/config.local.json` với `user: "nso_panel"`, mật khẩu vừa tạo, và đặt `"bootstrapSchema": false`. Cờ này ngăn panel thử `CREATE TABLE` ở mỗi lần chạy, để tài khoản giới hạn không cần đặc quyền `CREATE`. Nếu di chuyển sang database mới chưa có bảng `panel_*`, chạy **một lần** với tài khoản quản trị local và `bootstrapSchema: true`, rồi quay về tài khoản `nso_panel`. Không commit `config.local.json`, `data/`, `backups/` hay `reports/`.

Không thay đổi `bindHost` thành `0.0.0.0` trừ khi bạn hiểu rủi ro mạng LAN và đã thay đổi mật khẩu admin mạnh. Không mở MariaDB `3306` ra Internet.
