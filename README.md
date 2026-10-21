# Online Auction — Java 26

Khung dự án bài tập lớn theo đề: Client JavaFX + FXML, Server Spring Boot REST + WebSocket, MySQL.

## Công nghệ
- JDK 26
- Spring Boot 4.1.1 (hỗ trợ Java 26 theo tài liệu chính thức)
- JavaFX 26, FXML
- MySQL 8+
- Maven 3.9+
- JUnit 5

## Cấu trúc
- `server/`: REST API, nghiệp vụ, JPA, transaction, WebSocket broadcast
- `client/`: JavaFX UI, REST client, WebSocket listener
- `database/schema.sql`: schema tham khảo
- `.github/workflows/ci.yml`: build/test backend và build client

## 1. Kiểm tra môi trường
```bash
java -version
mvn -version
```
Cả `java -version` và `mvn -version` phải trỏ đến JDK 26. Cài Maven 3.9+ và MySQL 8+.

## 2. Tạo database
Chạy `database/schema.sql` bằng MySQL Workbench hoặc:
```sql
CREATE DATABASE online_auction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Sau đó cấu hình biến môi trường:
- `AUCTION_DB_URL=jdbc:mysql://localhost:3306/online_auction`
- `AUCTION_DB_USER=auction_user`
- `AUCTION_DB_PASSWORD=your_password`

Hoặc sửa `server/src/main/resources/application.properties` trên máy cá nhân (không commit mật khẩu thật).

## 3. Chạy server
```bash
cd server
mvn clean test
mvn spring-boot:run
```
Server mặc định tại `http://localhost:8080`.
Kiểm tra: `GET http://localhost:8080/api/auctions`.

## 4. Chạy JavaFX client
Mở terminal thứ hai:
```bash
cd client
mvn clean javafx:run
```
Client gọi REST tại `http://localhost:8080` và nghe WebSocket tại `ws://localhost:8080/ws/auctions`.

## 5. API khởi đầu
- `GET /api/auctions`
- `GET /api/auctions/{id}`
- `POST /api/auctions` — tạo phiên mẫu, cần sellerId hợp lệ trong bản hoàn thiện
- `POST /api/auctions/{id}/bids` — body `{ "bidderId": 2, "amount": 120.00 }`
- `POST /api/users/register`
- `GET /api/health`

## 6. Lưu ý quan trọng trước khi nộp
1. Đây là starter scaffold để nhóm phát triển, không phải sản phẩm thương mại hoàn chỉnh.
2. Mẫu ban đầu chưa triển khai JWT/session authentication; `bidderId` trong request chỉ phục vụ phát triển và phải thay bằng danh tính đã xác thực trước khi nộp.
3. Đăng ký hiện là khung minh họa và chưa có đăng nhập/xác thực đầy đủ. Trước khi nộp, triển khai BCryptPasswordEncoder (không dùng hashCode), endpoint đăng nhập, token/session và phân quyền ở server; kiểm tra quyền sở hữu Seller.
4. Đặt giá được khóa bằng `PESSIMISTIC_WRITE` trong transaction. Không dựa vào giá trên client.
5. Broadcast WebSocket chỉ chạy sau khi transaction hoàn tất. Khi client reconnect, luôn tải lại snapshot qua REST.
6. Bổ sung anti-sniping/auto-bidding/biểu đồ sau khi chức năng bắt buộc ổn định.
7. JavaFX chạy trên các hệ điều hành khác nhau cần dependency JavaFX native tương ứng; Maven OpenJFX xử lý phần lớn việc chọn dependency theo nền tảng.
