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
| `scripts/` | Script cài đặt, khởi động database, build/chạy và dừng server |
| `pom.xml` | Cấu hình Maven; đã cập nhật Lombok/compiler để build được với JDK 17 hoặc 21 |

Các chương trình Windows, MariaDB `winx64`, NetBeans, Notepad, WinRAR, archive nguồn lồng nhau và JAR build sẵn **không được đưa vào repository**. Maven sẽ tải dependency và tạo JAR trên thiết bị chạy server.

## Yêu cầu

Điện thoại cần cài Termux từ nguồn chính thức hoặc F-Droid, có bộ nhớ trống phù hợp với thư mục dữ liệu game và được phép chạy nền. Quy trình cài package của Termux sử dụng `pkg`, một wrapper của APT được khuyến nghị trong tài liệu Termux [3]. Server cần OpenJDK 17 trở lên, Maven, Git và MariaDB. Repository đặt bytecode mục tiêu Java 17 để tương thích rộng hơn; bản build đã được kiểm thử bằng JDK 21.

## Cài đặt toàn bộ bằng một lệnh

Mở Termux và dán đúng một lệnh sau:

```bash
curl -fsSL https://raw.githubusercontent.com/anhduc2003/Ninja-school-offline-/main/install.sh | bash
```

Lệnh này tự cập nhật package, cài curl/OpenJDK/Maven/MariaDB, tải một archive từ GitHub Release, tạo cấu hình, khởi tạo MariaDB, import SQL nếu database còn trống, build JAR và khởi động server ở chế độ headless. Script không cần quyền root. Nếu Termux yêu cầu quyền truy cập bộ nhớ Android, có thể chạy `termux-setup-storage` trước; thông thường dự án vẫn hoạt động trong thư mục `$HOME` mà không cần quyền này.

Nếu muốn cài vào thư mục khác, vẫn dùng một lệnh với biến môi trường:

```bash
curl -fsSL https://raw.githubusercontent.com/anhduc2003/Ninja-school-offline-/main/install.sh | INSTALL_DIR="$HOME/ninja-server" bash
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

## Build và chạy server

Khởi động server bằng:

```bash
bash scripts/start-server.sh
```

Lệnh này sẽ tự chạy `mvn -DskipTests package` nếu chưa có `target/Nso-jar-with-dependencies.jar`, sau đó chạy server với chế độ **headless** để không tạo cửa sổ Swing. Log nằm tại `logs/server.log` và PID nằm tại `.termux/server.pid`.

Theo dõi log:

```bash
tail -f logs/server.log
```

Mặc định server lắng nghe cổng `14444`. Nếu cần giảm hoặc tăng heap JVM theo RAM điện thoại, đặt `JAVA_OPTS` trước khi chạy, ví dụ:

```bash
JAVA_OPTS='-Xms128m -Xmx512m' bash scripts/start-server.sh
```

Dừng server bằng:

```bash
bash scripts/stop-server.sh
```

`start-server.sh` gọi `termux-wake-lock` nếu tiện ích đó có sẵn để hạn chế việc Android ngủ khi server đang chạy. Khi dừng server, script gọi `termux-wake-unlock` nếu có.

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
