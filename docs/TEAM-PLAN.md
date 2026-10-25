# Phân công 4 thành viên
1. **Thành viên 1 — User & Auth**: `User` abstract, `Bidder/Seller/Admin`, đăng ký/đăng nhập, kiểm tra vai trò, `AuthenticationException`, test.
2. **Thành viên 2 — Item & Auction**: `Item` abstract, `Electronics/Art/Vehicle`, `ItemFactory`, CRUD sản phẩm, tạo phiên, trạng thái phiên, `AuctionStore` phối hợp với thành viên 3.
3. **Thành viên 3 — Bidding & concurrency**: `Bid`, `AuctionManager` Singleton, kiểm tra giá, `ReentrantLock`, exceptions, kết thúc phiên/xác định người thắng, unit/concurrency tests.
4. **Thành viên 4 — GUI & networking**: JavaFX/FXML MVC, Socket client/server integration, Observer notification fan-out, UX, test tích hợp.

## Hợp đồng tích hợp
- Request/Response Serializable nằm trong `common.protocol`.
- Model dùng chung nằm trong `common.model`.
- Mọi thay đổi protocol/model phải được cả nhóm review trước khi merge.
- `main` chỉ chứa bản ổn định; phát triển trên `dev` và feature branches.

## Lộ trình theo hướng dẫn môn học
- Tuần 6: scope, GitHub, class diagram, OOP, Singleton, Factory, JavaFX Hello World.
- Tuần 7: Observer, nghiệp vụ đặt giá, trạng thái phiên, concurrency, Login/List GUI.
- Tuần 8: custom exceptions, JUnit, refactor SOLID, MVC/FXML.
- Tuần 9: Maven, Checkstyle, GitHub Actions, Socket và Serialization.
- Tuần 10: multi-client, Observer qua Socket, hoàn thiện các màn hình, đặt mục tiêu coverage >=60%.
- Tuần 11–12: tích hợp GUI–logic–network–data, test end-to-end nhiều client, sửa lỗi.
- Tuần 13–14: polish UI, test toàn hệ thống, chức năng nâng cao nếu kịp, README.
- Tuần 15: slide, demo server + nhiều client, mọi thành viên giải thích được code.
