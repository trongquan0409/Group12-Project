# Online Auction System — LTNC 2026 (Starter)

Bộ khung bám theo `Hướng dẫn BTL LTNC-2026.pdf`: Java 26, JavaFX/FXML, Java Socket, Serialization/File I/O, Observer, OOP, Singleton, Factory Method, MVC, JUnit, Maven, Checkstyle và GitHub Actions.

> Đây là starter scaffold để nhóm phát triển tiếp, không phải hệ thống hoàn thiện. Không có Spring Boot, WebSocket hoặc MySQL trong phương án B. Dữ liệu được lưu bằng Java Serialization vào file `data/auction-data.ser`.

## Yêu cầu
- JDK 26
- Maven 3.9+
- JavaFX Maven plugin tải các thư viện JavaFX qua Maven

## Cấu trúc
- `common/`: model dùng chung giữa client và server; DTO/protocol Serializable
- `server/`: Socket server, xử lý nhiều client, AuctionManager Singleton, persistence
- `client/`: JavaFX/FXML, MVC, Socket client và Observer UI update
- `docs/`: sơ đồ lớp, luồng, kế hoạch theo tuần và phân công

## Build và test
Tại thư mục gốc:
```bash
mvn clean test
```
Chạy server ở terminal 1:
```bash
mvn -pl server -am exec:java -Dexec.mainClass="ltnc.auction.server.AuctionServer"
```
Chạy client ở terminal 2:
```bash
mvn -pl client -am javafx:run
```
Nếu JavaFX plugin không nhận lệnh ở root, chạy `mvn javafx:run` trong thư mục `client` sau khi cài common: `mvn -pl common install`.

Mặc định server lắng nghe cổng `5050`; dữ liệu lưu ở `server/data/auction-data.ser`. Có thể đổi bằng biến môi trường `AUCTION_PORT` và `AUCTION_DATA_FILE`.

## Chức năng starter
- User abstract và Bidder/Seller/Admin
- Item abstract và Electronics/Art/Vehicle; ItemFactory (Factory Method đơn giản)
- Auction, Bid, AuctionManager (Singleton)
- Socket protocol bằng object serialization
- Observer thông báo khi có bid mới
- Khóa `ReentrantLock` theo phiên để tránh xử lý đặt giá đồng thời sai
- Custom exceptions
- Serialization lưu/tải state
- JavaFX/FXML MVC client cơ bản: tải danh sách phiên, đặt giá, hiển thị thông báo

## Các bước bắt buộc nhóm cần hoàn thiện
1. Hoàn thiện xác thực: starter chỉ minh họa luồng login, không dùng ngoài môi trường bài tập.
2. Thêm CRUD đầy đủ sản phẩm và màn hình Seller/Admin.
3. Hoàn thiện kết thúc phiên, xác định người thắng và xử lý PAID/CANCELED theo đặc tả nhóm.
4. Bổ sung test concurrency và persistence; đạt coverage >=60% bằng JaCoCo nếu giảng viên yêu cầu đo bằng công cụ.
5. Thêm Checkstyle và giữ GitHub Actions xanh.
6. Review bảo mật Java Serialization: chỉ deserialize dữ liệu từ peer tin cậy; với bài tập, giới hạn kết nối localhost.

## Git workflow
Dùng `main` và `dev`; mỗi thành viên tạo feature branch, commit theo Conventional Commits và mở PR để review.
