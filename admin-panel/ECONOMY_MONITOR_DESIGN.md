# Economy Monitor và hộp thư hệ thống

## Mục tiêu

Module **Kinh tế & cảnh báo** đọc dữ liệu thật theo `server.id` hiện tại, cung cấp tổng quan dòng tiền, các tín hiệu bất thường và hàng đợi review cho GM. Đây là hệ thống hỗ trợ điều tra; cảnh báo không tự động ban, kick hoặc thay đổi tài sản người chơi.

## Nguồn dữ liệu

| Nguồn | Sử dụng |
|---|---|
| `players` | Số người chơi, online, xu trong nhân vật, xu trong hộp, yên và số dư âm |
| `users` | Coin/lượng account, balance, username và cụm IP |
| `history_table` | Before/after coin-gold-yên và item trong cửa sổ thời gian; chỉ đầy đủ khi `open.historySQL=true` |
| `gift_code_histories` | Tần suất đổi gift code theo người chơi |
| `panel_reward_claims` | Tần suất nhận campaign reward |
| `shinwa` | Số listing và tổng giá trị kí gửi đang bán |
| `panel_player_inbox` | Số notification pending/delivered và lịch sử hộp thư |

Tổng xu và yên được CAST thành chuỗi ở SQL response để không mất độ chính xác khi số lớn. Các query đều giới hạn theo server hiện tại nếu dữ liệu có `server_id`.

## Rule cảnh báo

| Rule | Ngưỡng mặc định | Mức | Ý nghĩa |
|---|---:|---|---|
| `negative_balance` | Bất kỳ số dư âm | Critical | Dấu hiệu dữ liệu lỗi hoặc exploit cần kiểm tra ngay |
| `high_balance_concentration` | Từ 1.500.000.000 xu | Warning | Tài sản tập trung lớn; không phải kết luận gian lận |
| `large_balance_delta` | Biến động tuyệt đối từ 500.000.000 | Warning | Lấy từ before/after của `history_table` |
| `gift_claim_burst` | Từ 10 lượt trong cửa sổ | Warning | Đổi gift code dồn dập |
| `reward_claim_burst` | Từ 5 lượt trong cửa sổ | Warning | Nhận campaign reward dồn dập |
| `shared_ip_cluster` | Từ 5 account/IP | Info | Tín hiệu cần đối chiếu, có thể là gia đình/phòng máy |
| `shinwa_concentration` | Từ 20 listing hoặc 500.000.000 xu | Info | Tập trung giao dịch kí gửi |

Mỗi cảnh báo có `dedupe_key` theo rule, server, đối tượng và ngày UTC. Bảng `panel_economy_alerts` lưu evidence JSON, trạng thái `open`, `acknowledged`, `resolved` hoặc `false_positive`, cùng note review. Việc cập nhật trạng thái cần operator confirmation và audit.

## Hộp thư chuẩn hóa

`panel_player_inbox` lưu từng notification riêng biệt với title, body, category, source, source ID, dedupe key, delivery status, delivered time và read time. Notification Shinwa dùng `source_type=shinwa_expiry`, `source_id=shinwa.id` và dedupe key `shinwa-expiry:<server>:<shinwa_id>`.

Khi listing hết hạn, Java claim marker `panel_shinwa_expiry_notifications` và tạo inbox record trong cùng transaction. Seller online nhận alert ngay rồi record chuyển sang `delivered`; seller offline giữ `pending`. Khi seller chọn nhân vật sau login, Java đọc tối đa 20 record pending, hiển thị trong một hộp thư và đánh dấu `delivered/read` sau khi gửi client thành công. Luồng nhận lại item không bị thay đổi.

Kênh `players.message` cũ vẫn được giữ để tương thích các thông báo legacy. Notification mới của Shinwa dùng mailbox chuẩn hóa nhằm tránh gộp nhiều loại message vào một chuỗi và cho phép panel đối soát delivery.

## Cấu hình và giới hạn

Panel tự bootstrap các bảng `panel_economy_alerts` và `panel_player_inbox` khi `bootstrapSchema` bật. Launcher khởi động panel trước Java để schema có trước vòng lặp runtime. Nếu `open.historySQL=false`, panel vẫn hiển thị số dư hiện tại nhưng phải đánh dấu rõ rằng phân tích biến động lịch sử không đầy đủ.

Các rule hiện là ngưỡng cố định trong helper `lib/economy-monitor.mjs`, được trả về trong API để UI hiển thị minh bạch. Bước tiếp theo có thể đưa ngưỡng vào bảng cấu hình có version và phê duyệt, nhưng không nên cho phép thay đổi threshold mà không có audit.
