# Phân công nhóm 4 người

| Người | Owner | File/thư mục trọng tâm | Kết quả |
|---|---|---|---|
| 1 | Kiến trúc, tích hợp, đấu giá đồng thời | `AuctionService`, `AuctionRepository`, CI | bid transaction-safe, test concurrency, review PR |
| 2 | User, auth, schema | `UserAccount`, `UserService`, `UserController`, `database/` | đăng ký/đăng nhập thật, hash mật khẩu, phân quyền server |
| 3 | Item/Auction lifecycle | `Item`, CRUD, scheduler, lịch sử | tạo/sửa/xóa item/auction, kết thúc phiên đúng |
| 4 | JavaFX và realtime | `client/`, FXML, WebSocket | màn hình list/detail/seller, lỗi UI, nhận event realtime |

## Cách tích hợp
1. Người 1 tạo repo và skeleton, `main` luôn build được.
2. Mỗi người làm branch `feat/<tinh-nang>`, commit nhỏ, tạo Pull Request.
3. Không tự ý đổi DTO/endpoint; thay đổi API phải cập nhật `docs/API.md` và báo nhóm.
4. Mỗi PR cần có cách test và ít nhất một người review.
5. Mỗi ngày ghép code ít nhất một lần; không đợi tới cuối tuần.
6. Trước demo, một thành viên clone repo sạch và làm theo README để xác minh hướng dẫn chạy.

## Tiến độ đề xuất 4 tuần
- Tuần 1: schema, API contract, project build, GUI mock.
- Tuần 2: user, CRUD item/auction, danh sách/chi tiết.
- Tuần 3: bidding transaction, history, scheduler, WebSocket, JUnit.
- Tuần 4: security, concurrency test, CI, sửa lỗi, báo cáo và bảo vệ.
