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

Các thay đổi item, shop, monster và option có thể cần reload hoặc restart Java vì game giữ cache trong bộ nhớ. Chỉ số/hành trang nhân vật vẫn bị chặn khi người chơi online để tránh desync. Panel không tự dừng Java và không public MariaDB. Broadcast runtime là ngoại lệ có contract rõ ràng: bridge chỉ bind `127.0.0.1`, yêu cầu bearer token và chỉ gửi tin tới nhân vật online trong instance Java hiện tại.

## Reward Campaign Center

Mô-đun **Reward Campaigns** dùng để quản lý trực quan bốn nhóm quà: Fancung tại NPC Hùng Vương, quà tân thủ tại NPC Admin, nạp tích lũy tại NPC Hùng Vương và quà sự kiện tại NPC được chọn. Quản trị viên tìm item từ catalog, chọn option theo tên, đặt số lượng/min/max, tiền thưởng, thời gian hiệu lực, server scope và phạm vi nhận; không cần nhập JSON hay sửa mã Java.

Campaign được lưu trong các bảng chuẩn hóa `panel_reward_campaigns`, `panel_reward_items`, `panel_reward_item_options` và `panel_reward_claims`. Campaign có lượt nhận sẽ không thể sửa/xóa để giữ đối soát; chỉ có thể tắt. `server_id = 0` áp dụng mọi server, còn giá trị cụ thể chỉ áp dụng server tương ứng. Khi campaign active, menu hard-code tương ứng tại NPC được thay thế bằng campaign runtime.

| Loại campaign | NPC | Điều kiện runtime |
|---|---|---|
| Fancung | Hùng Vương | `users.fancung` đã đạt điều kiện |
| Quà tân thủ | Admin | `users.received_first_gift = 0` |
| Nạp tích lũy | Hùng Vương | `users.tongnap` đạt mốc Coin |
| Quà sự kiện | Admin hoặc Hùng Vương | `Char.eventPoint` theo key điểm và mốc cấu hình |

Mỗi campaign cần được bật sau khi kiểm tra preview. Runtime kiểm tra đủ slot túi, item/option tồn tại trong catalog, thời gian hiệu lực, điều kiện và claim history; các lượt nhận sau đó được ghi lại để tra cứu trong cùng module.

## Thông báo toàn server

Mô-đun **Thông báo** có hai luồng riêng. `options.notify` là cấu hình persisted mà Java có thể đọc khi khởi động; nút **Gửi thông báo ngay** dùng runtime bridge để gọi `GlobalService.chat` và phát `CMD.CHAT_SERVER` tới tất cả nhân vật đang online, không cần restart.

Để bật bridge, đặt các khóa sau trong `config.properties` thật của game rồi khởi động lại Java và panel:

```properties
server.control.host=127.0.0.1
server.control.port=18081
server.control.token=thay-bang-token-ngau-nhien-dai
```

Nếu `admin-panel/data/config.local.json` đã tồn tại từ trước, cập nhật thủ công phần `runtimeControl` tương ứng hoặc xóa riêng file cấu hình local rồi để panel tạo lại sau khi đã đặt token. Không commit token vào Git. Panel sẽ kiểm tra trạng thái bridge trước khi cho gửi; mỗi lần gửi cần nhập confirmation phrase dạng `BROADCAST NOTICE TEN_NGUOI_GUI` và được ghi vào audit.

Thông báo không được lưu để phát lại cho người chơi offline. Nếu Java đang tắt, sai token hoặc bridge chưa cấu hình, panel chỉ hiển thị trạng thái lỗi và khóa nút gửi.

## Thông báo Boss thế giới

Mô-đun **Thông báo Boss thế giới** quản lý thông báo tự động khi Boss xuất hiện hoặc bị hạ trong `SpawnBossManager`. Cấu hình được lưu trong `panel_world_boss_notifications`; lịch sử phát được ghi trong `panel_world_boss_notification_logs`. Panel tự tạo schema khi `bootstrapSchema` đang bật.

Trong form quản trị, chọn group Boss, loại sự kiện, server scope, người gửi, nội dung, cooldown và thời gian hiệu lực. `server_id = 0` áp dụng cho toàn bộ server; server cụ thể được ưu tiên hơn cấu hình global. Các placeholder được hỗ trợ là `{boss}`, `{map}`, `{zone}`, `{killer}` và `{time}`. Template lạ sẽ bị từ chối.

| Sự kiện | Thời điểm phát | Placeholder hữu ích |
|---|---|---|
| `spawn` | Ngay sau khi Boss được tạo tại khu ngẫu nhiên/lịch tương ứng | `{boss}`, `{map}`, `{zone}`, `{time}` |
| `defeat` | Khi Boss thế giới được người chơi hạ | `{boss}`, `{map}`, `{zone}`, `{killer}`, `{time}` |

Cooldown được áp dụng trong tiến trình Java để tránh phát lặp do retry hoặc nhiều trigger gần nhau. Nút **Test** gửi bản mẫu qua runtime bridge tới người chơi online nhưng không tạo kill/spawn giả và không ghi lịch sử gameplay. Thông báo mặc định khi chưa có cấu hình spawn vẫn được giữ lại để tương thích server cũ.

Quy trình setup là: đặt `server.control.token` trong `config.properties`, khởi động lại Java và panel, mở **Thông báo Boss thế giới**, tạo cấu hình `spawn`/`defeat`, lưu bằng confirmation phrase, bật cấu hình và dùng **Test** để kiểm tra. Nếu database hoặc bridge không sẵn sàng, runtime tự fallback về thông báo spawn mặc định và panel hiển thị lỗi thao tác test. Thiết kế chi tiết nằm tại [`WORLD_BOSS_NOTIFICATION_DESIGN.md`](WORLD_BOSS_NOTIFICATION_DESIGN.md).

## Icon vật phẩm

Panel hiển thị thumbnail từ chính sprite game `Data/Img/Small/1/Small{item.icon}.png`, cùng quy tắc filename Java dùng khi client yêu cầu icon. Thumbnail hiện ở catalog item, picker hành trang, item đang mang, tìm/đang chọn Shop NPC và kết quả/reward Gift Code. Endpoint local-only chỉ nhận số icon hợp lệ, chỉ đọc zoom `1`–`4` trong thư mục sprite allowlist và trả fallback ID khi file icon thiếu; browser không thể đọc file tùy ý ngoài asset game.

## Shop NPC trực quan

Mở **Cửa hàng** rồi chọn store NPC. Danh sách bên phải chỉ hiển thị hàng hóa của store đang chọn; dùng ô tìm để lọc theo tên hoặc ID item. Khi thêm/sửa hàng, bấm **Tìm/chọn item**, chọn loại tiền và giá, rồi cấu hình khóa, hệ, hạn dùng và option chỉ số bằng từng dòng. Không cần nhập `itemId`, `expire` mili-giây hay `Options JSON` thủ công.

Sau khi lưu/xóa/tạo store, backend vẫn yêu cầu confirmation phrase, xác thực item/store/option ID, lưu audit và báo cần reload/restart Java. Mapping NPC menu sang store do mã Java quyết định, nên danh sách NPC trong panel chỉ dùng để tham chiếu; không thay đổi mapping runtime từ browser.

## Chỉnh nhân vật và hành trang trực quan

Trong **Người chơi**, tìm nhân vật rồi chọn **Chỉnh nhân vật**. Chỉ số tiềm năng, kỹ năng, EXP, số ô túi/rương và bốn chỉ số potential được nhập bằng các ô số có nhãn; không cần nhập mảng JSON.

Phần **Hành trang trực quan** tách thành Túi đồ, Rương, Trang bị và Thời trang. Chọn **Thêm vật phẩm** hoặc **Đổi item**, tìm template trong catalog, rồi điều chỉnh từng dòng: vị trí ô, số lượng, khóa, system, upgrade, yên, hạn dùng và option chỉ số. Khi bấm lưu, panel tạo JSON theo contract Java, kiểm tra giới hạn slot, item/option tồn tại, loại trang bị, confirmation phrase, snapshot và audit. Nếu inventory JSON cũ không hợp lệ, editor thông báo section lỗi và không tự thay đổi dữ liệu.

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

Gift Code Control Center quản lý mã, phạm vi server, tiền tệ, reward nhiều vật phẩm, option chỉ số, khóa item, upgrade, thời hạn reward, thời điểm bắt đầu/kết thúc, quota tổng, tắt/bật thủ công và lịch sử đổi. Trình tạo mới đi theo **bốn bước**: mã/phạm vi, lịch/quota theo preset, reward từ catalog và rà soát. Mã có thể tạo tự động; item được tìm/chọn theo tên; option thêm theo từng dòng, vì vậy người quản trị không phải làm việc với ID rời hoặc cấu trúc kỹ thuật. **Java là nguồn quyết định**: khi người chơi nhập code, server kiểm tra lifecycle và tăng bộ đếm trong transaction có row lock. Vì vậy code tự đến lịch hoặc hết hạn ngay cả khi panel đang tắt; không có scheduler hoặc thao tác hot-switch nào cần chạy nền.

| Cơ chế | Hành vi runtime |
|---|---|
| `starts_at` | Từ chối code cho đến thời điểm bắt đầu. |
| `expires_at` | Từ chối code sau thời điểm hết hạn. |
| `disabled` | Tắt/bật code thủ công, không tái sử dụng cột `status` đã dành cho code toàn server đã tiêu thụ. |
| `max_redemptions` | Chặn khi đạt tổng lượt đổi; `type=0` luôn có quota 1. |
| `type=1` | Mỗi account chỉ đổi một lần, dựa trên `gift_code_histories`. |
| `expire_days` reward | Được tính từ lúc người chơi đổi code, không phải từ lúc quản trị viên tạo code. |

Từ launcher v1.4.6, `bash run-server.sh` tự kiểm tra migration lifecycle sau khi MariaDB local sẵn sàng và trước Java/panel. Migration là idempotent, chỉ thêm cột/index thiếu. Với database cũ hoặc khi log báo thiếu quyền `ALTER`, có thể chạy migration thủ công một lần bằng user MariaDB local có quyền phù hợp:

```bash
bash scripts/migrate-gift-code-lifecycle.sh
bash run-server.sh
```

Migration chỉ bổ sung `starts_at`, `max_redemptions`, `redemption_count`, `disabled` và index vào `gift_codes`; không xóa code, reward hoặc lịch sử đổi. Nếu auto-migration lỗi, xem `tail -n 50 logs/gift-code-migration.log`. Sau đó panel user quyền tối thiểu chỉ cần `SELECT, INSERT, UPDATE, DELETE`; không cần quyền ALTER ở các lần khởi động thường khi schema đã sẵn sàng.

Reward item được canonicalize trước khi lưu: item phải tồn tại trong bảng `item`, còn mọi option được builder chuẩn hóa sau khi chọn từ catalog `item_option`. Không thể sửa reward/lifecycle hay xóa code đã có redemption; hãy tắt code cũ và tạo code mới để audit/lịch sử đổi không bị sai lệch.

## Thao tác trực quan không dữ liệu kỹ thuật

Từ v1.4.8, **hành trang**, **Shop NPC**, **Gift Code** và **Event Control** đều dùng catalog, picker và builder theo dòng. Màn hành trang hiển thị số lượng/tóm tắt từng khu vực; Event Control chọn item rơi và tỷ lệ theo từng dòng; Shop NPC/Gift Code chọn item có icon và thêm option từ catalog. Phần lưu trữ cấu trúc game vẫn được panel chuẩn hóa nội bộ để tương thích Java, nhưng không còn là thao tác hoặc dữ liệu thô quản trị viên phải đọc/nhập.

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
