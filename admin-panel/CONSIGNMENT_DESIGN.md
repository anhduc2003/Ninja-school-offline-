# Quản lý kí gửi NPC Shinwa

## Phạm vi

Module **Kí gửi Shinwa** đọc trực tiếp bảng `shinwa` theo `server.id` trong `config.properties`. Panel giải mã item và options ở backend rồi trả về các trường hiển thị rõ ràng; quản trị viên không nhập hoặc chỉnh raw JSON.

## Dữ liệu hiển thị

Mỗi listing hiển thị ID tin, icon, tên và ID item, số lượng, khóa, hệ, cấp nâng cấp, yên, option theo tên/giá trị, người bán, server, giá xu, thời hạn còn lại và trạng thái. Trạng thái runtime gồm `0 = Đang bán`, `1 = Đã bán`, `2 = Đã nhận lại`.

## Thao tác được phép

Chỉ tin đang bán được chỉnh giá và thời hạn. Giá hợp lệ từ 1 đến 2.000.000.000 xu; thời hạn được nhập theo giây trong khoảng 1 giây đến 30 ngày. Admin có thể **Đánh dấu hết hạn** để đưa thời gian về 0, sau đó StallManager chuyển listing sang luồng chờ nhận lại theo logic game.

Panel không tạo listing giả, không thay seller/item, không sửa tin đã mua/đã nhận lại và không xóa trực tiếp lịch sử kí gửi. Mọi thay đổi cần confirmation phrase và được ghi audit.

## Đồng bộ runtime

Sau khi update database, panel gọi `/api/control/shinwa-sync` qua bearer token để cập nhật giá/thời gian/trạng thái trong `StallManager` nếu listing đang có trong cache Java. Nếu Java hoặc bridge không online, thay đổi database vẫn được lưu và audit; cần restart Java để cache đọc lại bảng `shinwa`.

## Bảo toàn contract game

Item JSON gốc chỉ được parse nội bộ bằng server-side helper để đọc thuộc tính. Panel không expose trường raw item cho form và không tạo JSON mới. Việc mua, trả tiền cho seller, nhận lại item hết hạn, trừ phí và ghi `History` vẫn do Java `Stall`/`StallManager` xử lý.

## Thông báo hết hạn vào hộp thư seller

Khi `Stall.update()` chuyển một item từ `productList` sang `expiredProductList`, runtime gọi luồng thông báo đúng một lần cho seller của listing. Nội dung thông báo gồm ID tin, tên vật phẩm và hướng dẫn gặp NPC Shinwa để nhận lại vật phẩm; seller không được suy ra từ người đang online khác hoặc từ dữ liệu panel.

Để chống gửi trùng qua nhiều tick, restart hoặc nhiều runtime xử lý đồng thời, claim được thực hiện bằng `INSERT IGNORE` vào bảng `panel_shinwa_expiry_notifications` với khóa chính kép `(shinwa_id, server_id)`. Marker lưu seller và `notified_at`; cùng transaction đó tạo một bản ghi trong `panel_player_inbox` với dedupe key theo server/listing. Bảng marker và inbox được tạo tự động bởi `admin-panel/server.mjs` và cũng có trong `SQL/nsoz.sql` cho cài đặt mới.

`panel_player_inbox` là hộp thư chuẩn hóa: seller online nhận private alert ngay và bản ghi chuyển sang `delivered`; seller offline giữ `pending`. Sau khi người chơi chọn nhân vật, `User` đọc tối đa 20 tin pending, gọi `showAlert("Hộp thư", ...)`, rồi đánh dấu `delivered/read`. Kênh `players.message` cũ vẫn được giữ cho thông báo legacy. Luồng `receiveItem()` không thay đổi: seller vẫn phải gặp NPC Shinwa, item chỉ bị xóa khỏi `shinwa` sau khi thêm thành công vào túi.

Panel hiển thị nhãn **Đã gửi hộp thư** hoặc **Chưa gửi hộp thư** trong bảng/chi tiết listing để admin kiểm tra mà không cần nhập JSON. Trạng thái này chỉ là quan sát; panel không tự nhận item thay người chơi.
