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
