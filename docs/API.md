# API contract v1

Base URL: `http://localhost:8080`

## Health
`GET /api/health`
Response: `{"status":"UP"}`

## Users
`POST /api/users/register`
```json
{
  "username": "bidder01",
  "password": "change-this-password",
  "displayName": "Bidder One",
  "role": "BIDDER"
}
```
Role hợp lệ khi đăng ký: `BIDDER` hoặc `SELLER`; không cho tự đăng ký `ADMIN`.

## Auctions
`GET /api/auctions`
`GET /api/auctions/{id}`
`POST /api/auctions`
```json
{
  "sellerId": 1,
  "itemName": "Laptop",
  "description": "Laptop used for demo",
  "startingPrice": 1000000,
  "minIncrement": 50000,
  "startTime": "2026-10-15T10:00:00Z",
  "endTime": "2026-10-15T11:00:00Z"
}
```
`GET /api/auctions/{id}/bids`
`POST /api/auctions/{id}/bids`
```json
{ "bidderId": 2, "amount": 1100000 }
```

## WebSocket
Connect to `ws://localhost:8080/ws/auctions`.
Event:
```json
{
  "type": "BID_UPDATED",
  "auctionId": 1,
  "currentPrice": 1100000,
  "leadingBidderId": 2,
  "endTime": "2026-10-15T11:00:00Z",
  "eventTime": "2026-10-15T10:12:00Z"
}
```

## Quy ước lỗi
- `400`: request sai hoặc vi phạm quy tắc nghiệp vụ
- `404`: nên bổ sung handler riêng cho tài nguyên không tồn tại
- `500`: lỗi không dự kiến; không trả stack trace cho client
