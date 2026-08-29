<p align="center">
  <img src="docs/assets/ninja-school-banner.png" alt="Ninja School Online" width="100%" />
</p>

<h1 align="center">Ninja School Offline</h1>

<p align="center">
  <strong>Máy chủ Ninja School chạy local trên Termux/Android, Linux và Windows</strong><br />
  Java game server · MariaDB · Ninja Control Room · Event & Reward Management
</p>

<p align="center">
  <a href="https://github.com/anhduc2003/Ninja-school-offline-/commits/main"><img src="https://img.shields.io/github/last-commit/anhduc2003/Ninja-school-offline-?style=flat-square&color=f5b942" alt="Last commit" /></a>
  <a href="https://github.com/anhduc2003/Ninja-school-offline-/commits/main"><img src="https://img.shields.io/github/commit-activity/m/anhduc2003/Ninja-school-offline-?style=flat-square&color=4f9cf9" alt="Commit activity" /></a>
  <a href="https://github.com/anhduc2003/Ninja-school-offline-/issues"><img src="https://img.shields.io/github/issues/anhduc2003/Ninja-school-offline-?style=flat-square&color=8b5cf6" alt="Issues" /></a>
  <img src="https://img.shields.io/badge/Java-17%2B-ef4444?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17+" />
  <img src="https://img.shields.io/badge/Termux-Android-22c55e?style=flat-square&logo=android&logoColor=white" alt="Termux Android" />
  <img src="https://img.shields.io/badge/Control_Room-local--only-06b6d4?style=flat-square" alt="Local only" />
</p>

<p align="center">
  <a href="#-bắt-đầu-trong-60-giây">Bắt đầu</a> ·
  <a href="#-ninja-control-room">Web panel</a> ·
  <a href="#-quản-lý-event-và-phần-thưởng">Event & Reward</a> ·
  <a href="admin-panel/README.md">Runbook</a>
</p>

> **Ninja School Offline** là bộ mã nguồn máy chủ game Java và công cụ vận hành local. GitHub chỉ lưu trữ/phân phối mã nguồn; server được build và chạy trên thiết bị của bạn, không chạy liên tục trên GitHub.

## ✨ Tổng quan

Repository này đóng gói một quy trình triển khai hoàn chỉnh cho server Ninja School: cài dependency, khởi tạo MariaDB, import schema an toàn, build Java bằng Maven, chạy headless và mở **Ninja Control Room** ngay trên cùng thiết bị. Mục tiêu là giúp người vận hành có thể cài đặt nhanh nhưng vẫn kiểm soát được dữ liệu, log, backup và thay đổi gameplay.

| Trụ cột | Có sẵn trong repository |
|---|---|
| **Game server** | Java server, map, mob, NPC, event, item và dữ liệu runtime trong `src/` và `Data/`. |
| **Database** | MariaDB/MySQL với schema khởi tạo tại `SQL/nsoz.sql`; script import chỉ chạy khi database còn trống. |
| **Web panel** | Ninja Control Room local-only với hơn 20 module quản trị, validation, confirmation phrase và audit trail. |
| **Reward engine** | Gift Code, Fancung, quà tân thủ, nạp tích lũy, quà event và lịch sử claim. |
| **Launcher** | Cài đặt một lệnh, kiểm tra MariaDB, build JAR, chạy headless, đồng bộ source và quản lý log/PID. |
| **Tương thích** | Termux/Android là luồng chính; Windows có script start/stop riêng; Linux có thể chạy các script Bash tương ứng. |

## 🧭 Mục lục

- [Bắt đầu trong 60 giây](#-bắt-đầu-trong-60-giây)
- [Chạy server trên Termux](#-chạy-server-trên-termux)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt đầy đủ](#-cài-đặt-đầy-đủ)
- [Cấu hình database](#-cấu-hình-database)
- [Lệnh vận hành](#-lệnh-vận-hành)
- [Ninja Control Room](#-ninja-control-room)
- [Quản lý Event và Phần thưởng](#-quản-lý-event-và-phần-thưởng)
- [Cây thư mục](#-cây-thư-mục)
- [Cập nhật source](#-cập-nhật-source)
- [Xử lý sự cố](#-xử-lý-sự-cố)
- [An toàn và giới hạn](#-an-toàn-và-giới-hạn)

## 🚀 Bắt đầu trong 60 giây

Trên Termux, chạy đúng một lệnh sau:

```bash
curl -fsSL https://github.com/anhduc2003/Ninja-school-offline-/releases/download/v1.4.8/install-v1.4.8.sh | bash
```

Sau khi cài xong, mở panel tại [`http://127.0.0.1:18080`](http://127.0.0.1:18080). Launcher sẽ tự cài package cần thiết, chuẩn bị MariaDB, import SQL nếu database chưa có bảng, build JAR và khởi động game ở chế độ headless.

> **Lưu ý:** Nếu Termux hỏi quyền truy cập bộ nhớ Android, có thể chạy `termux-setup-storage`. Thông thường server vẫn hoạt động trong thư mục `$HOME` mà không cần quyền này.

## 📱 Chạy server trên Termux

Đây là quy trình khuyến nghị khi cài từ source GitHub trên Termux. Mở ứng dụng Termux chính thức hoặc bản F-Droid, sau đó chạy từng khối lệnh sau:

### 1. Cài dependency và tải source

```bash
pkg update -y && pkg upgrade -y
pkg install -y git curl
cd "$HOME"
git clone https://github.com/anhduc2003/Ninja-school-offline-.git
cd Ninja-school-offline-
chmod +x run-server.sh scripts/*.sh admin-panel/*.sh
```

Nếu thư mục repository đã tồn tại, không chạy lại `git clone`; hãy cập nhật bằng:

```bash
cd "$HOME/Ninja-school-offline-"
git pull --ff-only origin main
```

### 2. Chuẩn bị Java, Maven, Node.js và MariaDB

```bash
bash scripts/setup-termux.sh
```

Script sẽ cài hoặc kiểm tra OpenJDK 17/21, Maven, Node.js, MariaDB; tạo datadir MariaDB local, cấu hình socket/port local và tạo `config.properties` từ file mẫu nếu file chưa tồn tại. Không commit file `config.properties` lên GitHub.

### 3. Khởi tạo database và build Java

```bash
bash scripts/init-db.sh
mvn -DskipTests package
```

`init-db.sh` chỉ import `SQL/nsoz.sql` khi database chưa có dữ liệu tương ứng; không dùng lệnh reset database người chơi. Có thể chạy kiểm tra đầy đủ trước khi mở server:

```bash
mvn test
```

### 4. Chạy toàn bộ game stack

```bash
bash run-server.sh
```

Launcher sẽ đồng bộ source an toàn, khởi động MariaDB, kiểm tra migration Gift Code, khởi động Ninja Control Room trước Java, sau đó chạy game server headless và scheduler. Các endpoint local sau khi khởi động thành công là:

| Thành phần | Địa chỉ |
|---|---|
| Game server | `127.0.0.1:14444` |
| Ninja Control Room | [`http://127.0.0.1:18080`](http://127.0.0.1:18080) |
| Runtime bridge | `127.0.0.1:18081` khi đã cấu hình token |
| MariaDB | `127.0.0.1:3306` |

Để tránh launcher tự đồng bộ source trong một lần chạy, dùng `NSO_AUTO_SYNC=0 bash run-server.sh`. Nếu muốn giới hạn bộ nhớ Java trên điện thoại yếu, dùng ví dụ `JAVA_OPTS='-Xms128m -Xmx512m' bash run-server.sh`.

### 5. Theo dõi và dừng server

Mở một phiên Termux khác để xem log:

```bash
cd "$HOME/Ninja-school-offline-"
tail -f logs/server.log
```

Dừng theo thứ tự an toàn:

```bash
cd "$HOME/Ninja-school-offline-"
bash scripts/stop-server.sh
bash admin-panel/stop-panel.sh
bash scripts/stop-db.sh
```

Kiểm tra nhanh trạng thái process và port mà không thay đổi dữ liệu:

```bash
ps -ef | grep -E 'java|mariadbd|server.mjs|scheduler.mjs' | grep -v grep
ss -ltn | grep -E ':(3306|14444|18080|18081)\\b'
```

### 6. Cho phép Termux chạy nền

Android có thể dừng tiến trình khi khóa màn hình hoặc bật tiết kiệm pin. Hãy mở **Settings → Apps → Termux → Battery**, chọn **Unrestricted/Không hạn chế** nếu thiết bị hỗ trợ. Khi launcher nhận diện được lệnh này, có thể giữ CPU thức trong lúc server chạy bằng:

```bash
termux-wake-lock
```

Sau khi dừng server hoàn toàn, nhả wake lock bằng:

```bash
termux-wake-unlock
```

> **Quan trọng:** Không public port `14444`, `18080`, `18081` hoặc `3306` trực tiếp ra Internet. Control Room và database được thiết kế local-only; nếu cần server 24/7 nên dùng máy Linux/hosting có giám sát thay vì điện thoại cá nhân.

## 🧰 Yêu cầu hệ thống

| Môi trường | Yêu cầu |
|---|---|
| **Termux/Android** | Termux từ nguồn chính thức hoặc F-Droid, OpenJDK 17+, Maven, Git, MariaDB và đủ dung lượng cho thư mục `Data/`. |
| **Windows** | Java 17+, Maven, Node.js, MariaDB service và Git Bash hoặc môi trường chạy script tương thích. |
| **Linux** | OpenJDK 17+, Maven, Git, MariaDB và các tiện ích Bash cơ bản. |

Repository đặt target bytecode Java 17 để tương thích rộng hơn; bản build đã được kiểm thử với JDK 21. Điện thoại cần được phép chạy nền nếu muốn server hoạt động lâu; Android có thể giới hạn CPU, pin, mạng hoặc tiến trình background.

## 📦 Cài đặt đầy đủ

### Cài vào thư mục mặc định

```bash
curl -fsSL https://github.com/anhduc2003/Ninja-school-offline-/releases/download/v1.4.8/install-v1.4.8.sh | bash
```

### Cài vào thư mục tùy chọn

```bash
curl -fsSL https://github.com/anhduc2003/Ninja-school-offline-/releases/download/v1.4.8/install-v1.4.8.sh | INSTALL_DIR="$HOME/ninja-server" bash
```

Script không cần quyền root. Khi chạy lại trên một bản cài hợp lệ, script dừng tiến trình cũ, kiểm tra archive trước khi thay source và giữ lại runtime local như `config.properties`, `.termux`, `logs`, `admin-panel/config.local.json`, backup và report. Script không tự import lại SQL vào database đã có bảng và không reset dữ liệu người chơi.

Nếu muốn tự thực hiện từng bước:

```bash
bash scripts/init-db.sh       # Khởi động MariaDB, tạo database và import SQL nếu còn trống
mvn test                      # Kiểm tra build/test Java
bash run-server.sh            # Khởi động MariaDB, game server, panel và scheduler
```

## 🗄️ Cấu hình database

Mở file cấu hình local:

```bash
nano config.properties
```

Cấu hình tối thiểu cho MariaDB local:

```properties
db.host=127.0.0.1
db.port=3306
db.user=root
db.password=
db.dbname=nsoz
db.driver=com.mysql.cj.jdbc.Driver
open.historySQL=true
```

Không commit `config.properties` lên GitHub. File này nằm trong `.gitignore`; chỉ `config.properties.example` được quản lý trong repository. Nếu dùng MariaDB ở máy khác, chỉ kết nối qua mạng riêng đáng tin cậy và không public database trực tiếp ra Internet.

## ▶️ Lệnh vận hành

| Tác vụ | Lệnh |
|---|---|
| Khởi động toàn bộ stack | `bash run-server.sh` |
| Khởi động bằng đường dẫn tuyệt đối | `bash ~/Ninja-school-offline-/run-server.sh` |
| Điều chỉnh heap JVM | `JAVA_OPTS='-Xms128m -Xmx512m' bash run-server.sh` |
| Theo dõi log game | `tail -f logs/server.log` |
| Dừng game server | `bash scripts/stop-server.sh` |
| Dừng MariaDB | `bash scripts/stop-db.sh` |
| Dừng panel và scheduler | `bash admin-panel/stop-panel.sh` |
| Khởi động Windows | `scripts\\windows-start-stack.cmd` |
| Dừng Windows | `scripts\\windows-stop-stack.cmd` |

Launcher mặc định dùng cổng game `14444` và panel `18080`. Log chính nằm tại `logs/server.log`, `logs/admin-panel.log`, `logs/mariadb.log`; PID game nằm tại `.termux/server.pid`.

## 🖥️ Ninja Control Room

Ninja Control Room là web panel chạy **local-only** trên `127.0.0.1:18080`. Panel không sử dụng login, password, cookie session hoặc tài khoản Admin game; mọi thao tác ghi vẫn cần CSRF local, confirmation phrase, validation, parameterized SQL và audit append-only.

> **Không mở cổng `18080` hoặc MariaDB ra LAN/Internet.** Bất kỳ ứng dụng nào chạy trên cùng thiết bị đều có thể truy cập endpoint local-only.

### Các nhóm quản trị

| Nhóm | Module tiêu biểu | Mục đích |
|---|---|---|
| **Người chơi** | Người chơi, Tài khoản, Kiểm duyệt, Túi đồ, Tiền tệ | Tra cứu, khóa/mở tài khoản và chỉnh state offline có kiểm soát. |
| **Nội dung** | Vật phẩm tùy biến, Cửa hàng, Event Control, Quái & Boss | Quản lý catalog, shop, event, metadata quái và asset. |
| **Phần thưởng** | Phần thưởng, Reward Campaigns, Lịch sử reward | Gift Code, Fancung, quà tân thủ, nạp tích lũy, event reward và đối soát claim. |
| **Vận hành** | Thông báo, Bảo trì, Sức khỏe server, Sự cố | Health check, runbook bảo trì, audit và broadcast runtime. |
| **Phân tích** | Bảng xếp hạng, Phân tích, Audit trail, Sao lưu | Theo dõi KPI, top, lịch sử thao tác và backup local. |

### Nguyên tắc áp dụng thay đổi

Các thao tác chỉnh item, shop, monster và option có thể cần reload cache hoặc restart Java vì game giữ dữ liệu trong bộ nhớ. Chỉnh nhân vật và hành trang chỉ cho phép khi nhân vật offline; dữ liệu JSON cũ lỗi sẽ bị khóa editor thay vì tự động ghi đè. Panel không giả vờ dừng Java chỉ bằng SQL: maintenance chỉ tạo draft, approval và runbook.

## 🎁 Quản lý Event và Phần thưởng

### Reward Campaign Center

Mục **Reward Campaigns** dùng wizard và catalog trực quan, không yêu cầu nhập JSON thủ công.

| Loại campaign | NPC | Điều kiện runtime |
|---|---|---|
| **Fancung** | Hùng Vương | Trạng thái Fancung của tài khoản/nhân vật. |
| **Quà tân thủ** | Admin | Cờ `received_first_gift` chưa được nhận. |
| **Nạp tích lũy** | Hùng Vương | Tổng nạp `tongnap` đạt mốc Coin cấu hình. |
| **Quà sự kiện** | NPC được chọn | `eventPoint` theo key và mốc điểm cấu hình. |

Trong wizard, quản trị viên có thể tìm item theo tên/ID từ catalog, chọn số lượng, khóa, system, upgrade, hạn ngày, option theo tên với min/max, Coin/Lượng/Yên, thời gian hiệu lực, server scope và claim scope. `server_id = 0` áp dụng toàn bộ server; campaign đã có claim chỉ được tắt để bảo toàn đối soát.

Claim được ghi vào các bảng chuẩn hóa `panel_reward_campaigns`, `panel_reward_items`, `panel_reward_item_options` và `panel_reward_claims`. Runtime kiểm tra thời gian, điều kiện, đủ slot túi, item/option tồn tại và `claim_key` duy nhất trước khi cấp quà.

### Gift Code Control

Gift Code Control quản lý code, tiền tệ, reward item/options, khóa/upgrade/hạn item, thời gian hiệu lực, quota tổng, bật/tắt và lịch sử redemption. Java kiểm tra lifecycle và tăng quota atomically; code tự hết hạn ngay cả khi panel tắt. Launcher tự kiểm tra migration lifecycle trước khi mở Java/panel.

### Event Control

Event Control tạo plan pending cho event class, thời hạn và drop table. Drop item phải tồn tại trong catalog; plan chỉ áp dụng sau khi Java được dừng rồi chạy lại. Event **Lễ hội Sao Đêm** có ID `9`, drop lồng đèn sao, đổi quà và bảng top `star_lantern`. Chi tiết giới hạn runtime nằm trong [`admin-panel/README.md`](admin-panel/README.md).

## 🗂️ Cây thư mục

```text
.
├── src/                         # Java game server
├── Data/                        # Map, mob, NPC, item sprite, effect và language asset
├── SQL/nsoz.sql                 # Schema/dữ liệu khởi tạo MariaDB
├── admin-panel/                 # Ninja Control Room, scheduler, test và runbook
│   ├── public/                  # Frontend web panel
│   ├── server.mjs               # Local API, schema panel và audit boundary
│   └── README.md                # Runbook vận hành chi tiết
├── item_roi/                    # Cấu hình vật phẩm theo event
├── scripts/                     # Init DB, start/stop, Windows runbook
├── config.properties.example    # Cấu hình game mẫu, không chứa secret
├── install.sh                   # Bootstrap cài đặt
├── run-server.sh                # Launcher toàn bộ stack
└── pom.xml                      # Maven build Java
```

## 🔄 Cập nhật source

Mỗi lần chạy `run-server.sh`, launcher kiểm tra `origin/main` trước khi MariaDB/JVM khởi động. Khi source local sạch và có thể fast-forward đến GitHub, launcher tự đồng bộ source, xóa JAR cũ để Maven build lại và làm mới marker dependency panel.

Nếu GitHub không kết nối được, source local có thay đổi chưa commit, history bị diverge hoặc Java đang chạy, launcher ghi lý do vào `logs/sync.log` rồi dùng source local hiện có. Cơ chế này không reset, không xóa database và không tự merge source không xác minh được.

Tắt auto-sync cho một lần chạy:

```bash
NSO_AUTO_SYNC=0 bash run-server.sh
```

Tắt riêng bootstrap metadata trên bản Release không có `.git`:

```bash
NSO_SYNC_BOOTSTRAP=0 bash run-server.sh
```

## 🩺 Xử lý sự cố

| Hiện tượng | Kiểm tra |
|---|---|
| MariaDB không khởi động | Xem `logs/mariadb.log`; bảo đảm không có instance cũ dùng cùng datadir và chạy lại `bash scripts/init-db.sh`. |
| Panel báo database offline | Kiểm tra `db.host`, `db.port`, credential trong `config.properties` và xem `logs/admin-panel.log`. |
| Java build lỗi | Kiểm tra JDK/Maven, chạy `mvn test` và xem dependency cache; repository target Java 17+. |
| Gift Code migration lỗi | Xem `logs/gift-code-migration.log`, bảo đảm user MariaDB có quyền `ALTER`, rồi chạy `bash scripts/migrate-gift-code-lifecycle.sh`. |
| Panel không cập nhật source | Xem `tail -n 50 logs/sync.log`; kiểm tra source local sạch và Java đã dừng. |
| Android dừng server nền | Giữ Termux không bị battery optimization, cân nhắc `termux-wake-lock` và theo dõi `logs/server.log`. |

## 🔐 An toàn và giới hạn

Không commit password, token runtime, `config.properties`, database local, backup hoặc log lên GitHub. Web panel và MariaDB chỉ nên bind local; nếu cần server public 24/7, nên dùng Linux/hosting riêng thay vì điện thoại cá nhân.

Lớp khởi động gốc có giao diện AWT/Swing. Trên Termux headless, cửa sổ Swing bị tắt nhưng luồng game server vẫn được khởi tạo; các chức năng quản trị cũ phụ thuộc cửa sổ Swing không dùng được trong chế độ này.

Bộ mã nguồn và dữ liệu lớn; GitHub cảnh báo với file trên 50 MiB và chặn file đơn trên 100 MiB. Nếu bổ sung binary lớn, nên dùng Git LFS hoặc GitHub Releases thay vì commit archive trực tiếp [1] [2].

## 📚 Tài liệu liên quan

- [`admin-panel/README.md`](admin-panel/README.md) — cấu hình panel, quyền, audit, event, reward và runbook vận hành.
- [`admin-panel/WINDOWS-RUNBOOK.md`](admin-panel/WINDOWS-RUNBOOK.md) — kiểm thử stack trên Windows.
- [`admin-panel/REWARD_CAMPAIGN_DESIGN.md`](admin-panel/REWARD_CAMPAIGN_DESIGN.md) — thiết kế Reward Campaign Center.
- [`config.properties.example`](config.properties.example) — danh sách cấu hình game mẫu.
- [`SQL/nsoz.sql`](SQL/nsoz.sql) — schema database khởi tạo.

## Tham chiếu

[1]: https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github "GitHub — About large files on GitHub"
[2]: https://docs.github.com/en/repositories/creating-and-managing-repositories/repository-limits "GitHub — Repository limits"
[3]: https://wiki.termux.dev/wiki/Package_Management "Termux Wiki — Package Management"
[4]: https://github.com/termux/termux-packages/issues/21556 "Termux packages — MariaDB initialization issue #21556"
