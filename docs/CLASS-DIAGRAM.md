# Sơ đồ lớp (PlantUML)

```plantuml
@startuml
abstract class Entity
abstract class User
class Bidder
class Seller
class Admin
abstract class Item
class Electronics
class Art
class Vehicle
class Auction
class BidTransaction
class AuctionService
interface AuctionRepository
interface BidRepository
class AuctionWebSocketHandler

Entity <|-- User
User <|-- Bidder
User <|-- Seller
User <|-- Admin
Entity <|-- Item
Item <|-- Electronics
Item <|-- Art
Item <|-- Vehicle
Auction "1" --> "1" Item
Auction "1" --> "*" BidTransaction
BidTransaction "*" --> "1" Bidder
AuctionService --> AuctionRepository
AuctionService --> BidRepository
AuctionService --> AuctionWebSocketHandler
@enduml
```

Ghi chú: starter code hiện lưu `UserAccount` và `Item` dưới dạng entity JPA đơn giản để giảm độ phức tạp mapping. Nhóm có thể chuyển thành hierarchy `User`/`Bidder`/`Seller`/`Admin` và `Item`/`Electronics`/`Art`/`Vehicle` để thể hiện Inheritance/Polymorphism rõ hơn trong rubric, nếu triển khai đầy đủ và có ý nghĩa.
