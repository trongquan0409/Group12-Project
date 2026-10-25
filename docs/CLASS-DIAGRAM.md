# Sơ đồ lớp
```mermaid
classDiagram
 class User {<<abstract>> -String id -String username -String passwordHash +getRole()* }
 User <|-- Bidder
 User <|-- Seller
 User <|-- Admin
 class Item {<<abstract>> -String id -String name -BigDecimal reservePrice +getCategory()* }
 Item <|-- Electronics
 Item <|-- Art
 Item <|-- Vehicle
 class Auction { -String id -Item item -AuctionStatus status -BigDecimal highestPrice -List~Bid~ bids }
 class Bid { -String id -String bidderId -BigDecimal amount -Instant createdAt }
 class AuctionManager {<<Singleton>> -static AuctionManager INSTANCE +getInstance() +placeBid() +addObserver() }
 class AuctionObserver {<<interface>> +onAuctionEvent() }
 class ItemFactory {<<Factory>> +create() }
 class AuctionStore { +load() +save() }
 Auction "1" o-- "1..*" Bid
 Auction --> Item
 Bid --> Bidder
 AuctionManager --> Auction
 AuctionManager --> AuctionStore
 AuctionManager --> AuctionObserver
 ItemFactory ..> Item
```
Ghi chú: `AuctionManager` Singleton giúp thống nhất trạng thái trong một tiến trình server. `ReentrantLock` theo từng phiên bảo vệ thao tác đặt giá. `AuctionObserver` là điểm mở rộng để server thông báo sự kiện cho các client; starter hiện chưa triển khai push event qua kết nối client đang mở.
