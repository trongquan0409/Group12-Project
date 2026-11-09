# Hệ thống đấu giá trực tuyến

Đồ án môn **Lập trình nâng cao (LTNC) 2026** của Nhóm 12.

Dự án xây dựng bộ khung cho một ứng dụng đấu giá trực tuyến gồm máy chủ xử lý phiên đấu giá và ứng dụng JavaFX dành cho
người dùng. Hiện tại, repository đang ở giai đoạn starter scaffold; nhóm sẽ tiếp tục hoàn thiện chức năng, kiểm thử và
giao diện theo yêu cầu đồ án.

## Công nghệ sử dụng

- **Java 26**
- **Maven** để quản lý module, thư viện và quá trình build
- **JavaFX và FXML** cho giao diện ứng dụng
- **Java Socket** cho giao tiếp giữa client và server
- **Java Serialization và File I/O** để truyền đối tượng và lưu dữ liệu
- **JUnit 5** cho kiểm thử

Dự án sử dụng kiến trúc client-server, áp dụng các nội dung OOP và một số mẫu thiết kế như MVC, Observer, Singleton và
Factory Method.

> Dự án hiện không sử dụng Spring Boot, WebSocket hoặc MySQL. Dữ liệu đấu giá được lưu bằng Java Serialization vào file.

## Cấu trúc repository

```text
Group12-Project/
├── client/       # Ứng dụng JavaFX, FXML, MVC và kết nối đến server
├── common/       # Model, DTO và giao thức dùng chung giữa client và server
├── config/       # Tệp cấu hình của dự án
├── database/     # Tài nguyên liên quan đến dữ liệu
├── docs/         # Tài liệu thiết kế, sơ đồ và kế hoạch dự án
├── server/       # Socket server, xử lý đấu giá và lưu dữ liệu
├── pom.xml       # Cấu hình Maven tổng và danh sách module
└── README.md
```

## Các chức năng trong bộ khung hiện tại

Repository hiện mô tả các thành phần nền tảng sau:

- Các loại người dùng `Bidder`, `Seller` và `Admin`
- Các loại sản phẩm như `Electronics`, `Art` và `Vehicle`
- Các đối tượng phiên đấu giá và lượt đặt giá
- `AuctionManager` theo mẫu Singleton
- `ItemFactory` theo mẫu Factory Method đơn giản
- Giao tiếp client-server qua Java Socket bằng object serialization
- Observer để thông báo khi có lượt đặt giá mới
- Khóa `ReentrantLock` theo phiên để kiểm soát các lượt đặt giá đồng thời
- Lưu và tải trạng thái bằng Java Serialization
- Giao diện JavaFX/FXML cơ bản để tải danh sách phiên, đặt giá và hiển thị thông báo

Các mục trên thể hiện phạm vi starter được mô tả trong repository; cần kiểm tra mã nguồn và chạy ứng dụng để xác nhận
tình trạng thực tế của từng chức năng.

## Yêu cầu môi trường

- JDK 26
- Maven 3.9 trở lên

JavaFX được cấu hình để Maven tải các thư viện cần thiết.

## Build và chạy

Mở terminal tại thư mục gốc của repository.

### Build và chạy kiểm thử

```bash
mvn clean test
```

Checkstyle được chạy tự động ở bước `validate` khi build Maven, bao gồm cả mã nguồn chính và mã kiểm thử. Có thể chạy riêng bằng:

```bash
mvn checkstyle:check
```

### Chạy server

Mở một terminal riêng và chạy:

```bash
mvn -pl server -am compile exec:java
```

Lớp main của server (`ltnc.auction.server.net.AuctionServer`) đã được cấu hình sẵn trong `server/pom.xml`, không cần
truyền `-Dexec.mainClass`. Bước `compile` là bắt buộc để Maven biên dịch `common` và `server` trước khi chạy.

### Chạy client

Mở terminal khác và chạy:

```bash
mvn -pl client -am compile javafx:run
```

> Plugin `exec` và `javafx` được khai báo ở `pom.xml` gốc với `skip=true` để Maven nhận ra prefix `exec:`/`javafx:` ở mọi
> module khi dùng `-am`; chỉ `server` (exec) và `client` (javafx) bật lại plugin tương ứng.

Server mặc định lắng nghe tại cổng `5050`. Đường dẫn lưu dữ liệu mặc định là `server/data/auction-data.ser`.

Có thể cấu hình lại bằng các biến môi trường:

- `AUCTION_PORT`: cổng server
- `AUCTION_DATA_FILE`: đường dẫn file dữ liệu

Nếu lệnh JavaFX không chạy từ thư mục gốc, có thể cài module `common` trước:

```bash
mvn -pl common install
```

Sau đó chuyển vào thư mục `client` và chạy:

```bash
mvn javafx:run
```

## Công việc dự kiến tiếp tục hoàn thiện

1. Hoàn thiện xác thực và xử lý đăng nhập phù hợp với yêu cầu đồ án.
2. Bổ sung đầy đủ chức năng quản lý sản phẩm và màn hình dành cho Seller/Admin.
3. Hoàn thiện quy trình kết thúc phiên đấu giá, xác định người thắng và xử lý các trạng thái thanh toán hoặc hủy.
4. Bổ sung kiểm thử cho xử lý đồng thời và lưu/tải dữ liệu.
5. Bổ sung kiểm tra chất lượng mã và duy trì quy trình build ổn định.
6. Rà soát việc sử dụng Java Serialization; chỉ trao đổi dữ liệu với client đáng tin cậy trong phạm vi bài tập.

Mức độ hoàn thành thực tế của từng mục nên được cập nhật theo mã nguồn, kết quả chạy và tiến độ của nhóm.

## Quy trình làm việc với Git

Quy trình được đề xuất cho nhóm:

1. Cập nhật nhánh `dev` trước khi bắt đầu công việc.
2. Tạo nhánh riêng cho từng tính năng hoặc phần sửa lỗi.
3. Commit các thay đổi theo từng phần nhỏ, với nội dung commit rõ ràng.
4. Mở Pull Request từ nhánh tính năng vào `dev` để nhóm xem xét.
5. Chỉ đưa thay đổi đã được kiểm tra và thống nhất lên `main`.

Ví dụ tên nhánh:

```text
feature/login
feature/auction-management
fix/bid-validation
```

Ví dụ commit:

```text
feat: add bidder login flow
fix: validate bid amount
docs: update project setup instructions
```

## Tài liệu

Các tài liệu thiết kế, sơ đồ, kế hoạch và phân công của nhóm được lưu trong thư mục [`docs/`](docs/).
