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

## Event Control Center

Mô-đun **Event Control** quản lý catalog event Java, lớp cấu hình, thời hạn dùng chung, preview `event_points`, bảng top và nguồn vật phẩm rơi. Panel không chuyển event nóng: thay vào đó nó lưu một **plan pending** có audit, và plan chỉ được launcher áp dụng trước lần Java khởi động tiếp theo.

| Loại event | Panel có thể làm | Giới hạn |
|---|---|---|
| `json-drop` | Preview và lưu drop JSON vào plan; validator yêu cầu mọi `id` đã tồn tại trong bảng `item`. | Chỉ Java nạp override sau restart. |
| `code-drop` | Hiển thị catalog, class, điểm/top và trạng thái cấu hình. | Không ghi đè công thức hard-code trong Java. |
| `legacy` | Hiển thị cảnh báo ID chưa gán riêng. | Chỉ bật sau khi đã QA event trên bản sao dữ liệu. |
| `safe-off` | Lưu plan tắt event bằng `Exe_Z.event.OFF`. | Không xóa lịch sử điểm event cũ. |

Quy trình áp dụng có chủ đích là: chọn event và hạn kết thúc, xem lại drop JSON, nhập confirmation phrase `QUEUE EVENT TEN_EVENT`, sau đó dừng Java và khởi động lại:

```bash
bash scripts/stop-server.sh
bash run-server.sh
```

`run-server.sh` tạo bản sao `config.properties` trong `admin-panel/data/event-control/config-backups/`, ghi config từ plan, ghi override JSON (nếu event hỗ trợ), rồi xóa plan pending sau khi thành công. Không có endpoint panel nào tự dừng hoặc hot-switch Java. Trên Windows, `scripts\windows-start-stack.cmd` cũng áp dụng pending plan trước khi mở Java.

### Lễ hội Sao Đêm

Event mới `Exe_Z.event.StarFestival` dùng event ID `9`, asset `item_roi/event_StarFestival/STAR_FESTIVAL.json` và cùng contract với event hiện có. Quái có thể rơi lồng đèn ngôi sao cùng các phần thưởng mặc định; người chơi đổi **10 lồng đèn ngôi sao** lấy **1 Huyền tinh ngọc**, đồng thời cộng điểm top `star_lantern`. Nó không thêm boss/map effect runtime mới, nên có thể bật bằng cơ chế pending apply mà không đòi hỏi hot-switch map.

## Gift Code Control Center

Gift Code Control Center quản lý mã, phạm vi server, tiền tệ, reward nhiều vật phẩm, option chỉ số, khóa item, upgrade, thời hạn reward, thời điểm bắt đầu/kết thúc, quota tổng, tắt/bật thủ công và lịch sử đổi. **Java là nguồn quyết định**: khi người chơi nhập code, server kiểm tra lifecycle và tăng bộ đếm trong transaction có row lock. Vì vậy code tự đến lịch hoặc hết hạn ngay cả khi panel đang tắt; không có scheduler hoặc thao tác hot-switch nào cần chạy nền.

| Cơ chế | Hành vi runtime |
|---|---|
| `starts_at` | Từ chối code cho đến thời điểm bắt đầu. |
| `expires_at` | Từ chối code sau thời điểm hết hạn. |
| `disabled` | Tắt/bật code thủ công, không tái sử dụng cột `status` đã dành cho code toàn server đã tiêu thụ. |
| `max_redemptions` | Chặn khi đạt tổng lượt đổi; `type=0` luôn có quota 1. |
| `type=1` | Mỗi account chỉ đổi một lần, dựa trên `gift_code_histories`. |
| `expire_days` reward | Được tính từ lúc người chơi đổi code, không phải từ lúc quản trị viên tạo code. |

Với database đã tồn tại trước bản Gift Code lifecycle, chạy migration **một lần** bằng user MariaDB có quyền `ALTER` sau khi dừng Java game:

```bash
bash scripts/stop-server.sh
bash scripts/migrate-gift-code-lifecycle.sh
bash run-server.sh
```

Migration chỉ bổ sung `starts_at`, `max_redemptions`, `redemption_count`, `disabled` và index vào `gift_codes`; không xóa code, reward hoặc lịch sử đổi. Sau đó panel user quyền tối thiểu chỉ cần `SELECT, INSERT, UPDATE, DELETE`; không cần quyền ALTER ở các lần khởi động thường.

Reward item được canonicalize trước khi lưu: item ID phải tồn tại trong bảng `item`, option có dạng `[[optionId,value]]` và option ID phải có trong `item_option`. Không thể sửa reward/lifecycle hay xóa code đã có redemption; hãy tắt code cũ và tạo code mới để audit/lịch sử đổi không bị sai lệch.

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
