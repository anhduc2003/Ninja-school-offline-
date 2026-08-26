# Ninja School Offline — Server Termux Android

Repository này chứa phần mã nguồn server Java của Ninja School Offline được trích từ `NSO_SETUP_SERVER_FREE.zip`. **GitHub chỉ lưu trữ và phân phối mã nguồn; server không chạy liên tục trên GitHub.** Installer tải archive từ GitHub Release, sau đó build và chạy server trực tiếp trên điện thoại Android bằng Termux.

## Thành phần đã tích hợp

| Thành phần | Mục đích |
|---|---|
| `src/` | Mã nguồn Java của game server |
| `Data/` | Dữ liệu bản đồ, hình ảnh, hiệu ứng và ngôn ngữ cần khi server chạy |
| `SQL/nsoz.sql` | Schema và dữ liệu khởi tạo cho MariaDB/MySQL |
| `item_roi/` | Các cấu hình vật phẩm theo sự kiện |
| `config.properties.example` | Cấu hình mẫu, không chứa mật khẩu cá nhân |
| `install.sh` | Bootstrap toàn bộ quá trình cài đặt và khởi động bằng một lệnh |
| `run-server.sh` | Khởi động MariaDB và game server bằng một lệnh |
| `scripts/` | Script cài đặt, khởi động database, build/chạy và dừng server |
| `admin-panel/` | Web panel offline Node.js, RBAC local, audit SQL, scheduler và hơn 20 mô-đun quản trị |
| `pom.xml` | Cấu hình Maven; đã cập nhật Lombok/compiler để build được với JDK 17 hoặc 21 |

Các chương trình Windows, MariaDB `winx64`, NetBeans, Notepad, WinRAR, archive nguồn lồng nhau và JAR build sẵn **không được đưa vào repository**. Maven sẽ tải dependency và tạo JAR trên thiết bị chạy server.

## Yêu cầu

Điện thoại cần cài Termux từ nguồn chính thức hoặc F-Droid, có bộ nhớ trống phù hợp với thư mục dữ liệu game và được phép chạy nền. Quy trình cài package của Termux sử dụng `pkg`, một wrapper của APT được khuyến nghị trong tài liệu Termux [3]. Server cần OpenJDK 17 trở lên, Maven, Git và MariaDB. Repository đặt bytecode mục tiêu Java 17 để tương thích rộng hơn; bản build đã được kiểm thử bằng JDK 21.

## Cài đặt toàn bộ bằng một lệnh

Mở Termux và dán đúng một lệnh sau:

```bash
curl -fsSL https://github.com/anhduc2003/Ninja-school-offline-/releases/download/v1.1.0/install-v1.1.0.sh | bash
```

Lệnh này tự cập nhật package, cài curl/OpenJDK/Maven/MariaDB, tải một archive từ GitHub Release, tạo cấu hình, khởi tạo MariaDB, import SQL nếu database còn trống, build JAR và khởi động server ở chế độ headless. Script không cần quyền root. Nếu Termux yêu cầu quyền truy cập bộ nhớ Android, có thể chạy `termux-setup-storage` trước; thông thường dự án vẫn hoạt động trong thư mục `$HOME` mà không cần quyền này.

Nếu muốn cài vào thư mục khác, vẫn dùng một lệnh với biến môi trường:

```bash
curl -fsSL https://github.com/anhduc2003/Ninja-school-offline-/releases/download/v1.1.0/install-v1.1.0.sh | INSTALL_DIR="$HOME/ninja-server" bash
```

Script tải lại Release khi chạy lại; nếu thư mục đích là bản cài đặt cũ, script dừng server, giữ lại `config.properties`, rồi thay mã nguồn/runtime bằng archive mới. Nếu thư mục đích không phải bản cài đặt của server, script dừng để tránh ghi đè dữ liệu. Sau lần cài đầu tiên, bạn có thể xem log bằng `tail -f ~/Ninja-school-offline-/logs/server.log` và dừng server bằng `bash ~/Ninja-school-offline-/scripts/stop-server.sh`.

## Cấu hình database

Mở cấu hình bằng `nano`:

```bash
nano config.properties
```

Cấu hình tối thiểu cho MariaDB local là:

```properties
db.host=127.0.0.1
db.port=3306
db.user=root
db.password=
db.dbname=nsoz
db.driver=com.mysql.cj.jdbc.Driver
open.historySQL=true
```

Không commit `config.properties` lên GitHub. File này đã nằm trong `.gitignore`; chỉ `config.properties.example` được quản lý trong repository. Nếu sử dụng mật khẩu hoặc database từ máy khác, hãy thay đổi các trường tương ứng và bảo đảm thiết bị có thể kết nối tới máy đó.

## Khởi tạo database lần đầu

Chạy:

```bash
bash scripts/init-db.sh
```

Script sẽ khởi động MariaDB, tạo database `nsoz` nếu chưa tồn tại và import `SQL/nsoz.sql` **chỉ khi database chưa có bảng**. Khi database đã có bảng, script bỏ qua import để tránh ghi đè dữ liệu người chơi. Vì vậy, trước khi import vào database đang sử dụng, hãy sao lưu dữ liệu nếu cần. Bản SQL trong repository đã loại tài khoản `admin` mẫu có mật khẩu rõ ràng từ archive gốc; hãy tạo tài khoản riêng qua quy trình đăng ký/web hiện có hoặc công cụ quản trị của bạn, rồi đặt mật khẩu mạnh.

Bản cài đặt cũng tạo `.termux/mariadb.cnf` với `feedback=OFF`, `feedback_url=` và `innodb_use_native_aio=0`. MariaDB được khởi động bằng `mariadbd-safe` thay vì `--daemonize`, phù hợp với lệnh khởi động mà gói Termux in ra. Thiết lập này xử lý lỗi MariaDB trên một số bản Termux/Android khi feedback plugin không lấy được MAC address và làm InnoDB khởi tạo thất bại [4].

## Lệnh chạy server game

Sau khi cài đặt xong, dùng lệnh sau để khởi động cả MariaDB và game server:

```bash
bash ~/Ninja-school-offline-/run-server.sh
```

Nếu đang đứng trong thư mục dự án, có thể dùng:

```bash
bash run-server.sh
```

Launcher sẽ tự kiểm tra MariaDB, build JAR nếu chưa có, rồi chạy server ở chế độ **headless** để không tạo cửa sổ Swing. Nó cũng khởi động Ninja Control Room và scheduler local. Log game nằm tại `~/Ninja-school-offline-/logs/server.log`, log panel nằm tại `~/Ninja-school-offline-/logs/admin-panel.log`, và PID game nằm tại `~/Ninja-school-offline-/.termux/server.pid`.

Theo dõi log:

```bash
tail -f logs/server.log
```

Mặc định server lắng nghe cổng `14444`. Nếu cần giảm hoặc tăng heap JVM theo RAM điện thoại, đặt `JAVA_OPTS` trước khi chạy, ví dụ:

```bash
JAVA_OPTS='-Xms128m -Xmx512m' bash run-server.sh
```

Dừng game server bằng:

```bash
bash scripts/stop-server.sh
```

Nếu cần dừng cả MariaDB:

```bash
bash scripts/stop-db.sh
```

`start-server.sh` gọi `termux-wake-lock` nếu tiện ích đó có sẵn để hạn chế việc Android ngủ khi server đang chạy. Khi dừng server, script gọi `termux-wake-unlock` nếu có.

## Web panel offline: Ninja Control Room

Sau khi gọi `run-server.sh`, panel được mở cùng máy với game server tại:

```text
http://127.0.0.1:18080
```

Panel mặc định bind `127.0.0.1`, vì vậy không public cổng quản trị hoặc MariaDB ra Internet. Lần chạy đầu tiên, panel tạo local admin `admin` với mật khẩu ngẫu nhiên, chỉ ghi trong `admin-panel/data/first-login.txt` có quyền file hạn chế. Đăng nhập, đổi mật khẩu ngay sau lần dùng đầu tiên, rồi xoá file mật khẩu tạm thời.

Panel có 21 mô-đun được phân nhóm cho người chơi, tài khoản, moderation, inventory, currency, rewards, custom item, shop, event, rate, monster/boss, notice, maintenance, health, incident, audit, backup, leaderboard, analytics và jobs. Các thao tác có write hiện được map vào schema SQL thật của game như `users`, `players`, `item`, `shopcoin_tb1`, `monster`, `gift_codes` và `options`; chúng dùng parameterized query, transaction, confirmation phrase và audit append-only. Các thao tác item/shop/monster/options có thể cần restart hoặc reload cache Java để áp dụng vào tiến trình game đang giữ dữ liệu trong bộ nhớ.

Lịch local chạy bằng `admin-panel/start-scheduler.sh`, được launcher gọi sau panel. Job khởi tạo ở trạng thái **draft/disabled**; chỉ admin có thể phê duyệt và bật trong trang `Tác vụ định kỳ`. Scheduler chỉ tự thực thi `health_check`, `daily_report` và `cleanup`. Event hoặc maintenance transition được ghi audit là blocked cho đến khi có runbook nghiệp vụ riêng, tránh thay đổi gameplay không được đánh giá.

Để dừng riêng panel và scheduler:

```bash
bash admin-panel/stop-panel.sh
```

Chi tiết cấu hình local, role, cổng và giới hạn vận hành xem tại [`admin-panel/README.md`](admin-panel/README.md).

Trên Windows đã cài Java 17+, Maven, Node.js và MariaDB service, có thể dùng:

```bat
scripts\windows-start-stack.cmd
```

Script mặc định dùng Windows service tên `MariaDB`; nếu service dùng tên khác, đặt biến `NSO_MYSQL_SERVICE` trước khi chạy. Dừng Java/panel/scheduler bằng `scripts\windows-stop-stack.cmd`; script sẽ hỏi riêng trước khi thực thi `net stop` cho MariaDB, tránh cắt database ngoài ý muốn. Runbook kiểm thử Windows nằm tại [`admin-panel/WINDOWS-RUNBOOK.md`](admin-panel/WINDOWS-RUNBOOK.md).

## Cập nhật mã nguồn từ GitHub Release

Chạy lại đúng lệnh cài đặt một dòng để tải bản Release mới nhất mà installer đang chỉ tới. Script giữ lại `config.properties`, dừng tiến trình cũ trước khi thay runtime và không import lại SQL nếu database đã có bảng. Khi thay đổi schema, không tự động import lại SQL vào database đang có dữ liệu; hãy đánh giá migration riêng.

## Lưu ý quan trọng

Lớp khởi động gốc tạo giao diện AWT/Swing. Termux thông thường không có display server, nên repository đã thêm kiểm tra `GraphicsEnvironment.isHeadless()` và cờ `-Dninja.headless=true`; khi chạy trên Termux, cửa sổ quản trị bị tắt nhưng luồng game server vẫn được khởi tạo. Các chức năng quản trị trước đây nằm trong cửa sổ Swing sẽ không sử dụng được ở chế độ này.

Bộ mã nguồn và dữ liệu hiện chiếm khoảng 298 MB trong working tree. GitHub cảnh báo với file lớn hơn 50 MiB và chặn file đơn lớn hơn 100 MiB; repository này đã loại các archive/JAR lớn và không có file riêng nào vượt giới hạn nói trên [1]. GitHub khuyến nghị repository nhỏ hơn 1 GB và strongly recommends dưới 5 GB [1]. Nếu bổ sung binary lớn trong tương lai, dùng Git LFS hoặc GitHub Releases thay vì commit archive trực tiếp [1] [2].

Server game và MariaDB chạy trên điện thoại sẽ tiêu thụ CPU, RAM, pin và dung lượng lưu trữ. Android có thể dừng tiến trình nền hoặc thu hồi mạng khi Termux bị giới hạn; để server công khai ổn định 24/7, nên dùng máy chủ Linux/hosting riêng thay vì phụ thuộc vào điện thoại cá nhân.

## Tham chiếu

[1]: https://docs.github.com/en/repositories/working-with-files/managing-large-files/about-large-files-on-github "GitHub — About large files on GitHub"

[2]: https://docs.github.com/en/repositories/creating-and-managing-repositories/repository-limits "GitHub — Repository limits"

[3]: https://wiki.termux.dev/wiki/Package_Management "Termux Wiki — Package Management"

[4]: https://github.com/termux/termux-packages/issues/21556 "Termux packages — MariaDB initialization issue #21556"
