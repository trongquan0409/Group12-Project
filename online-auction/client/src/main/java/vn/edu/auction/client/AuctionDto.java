package vn.edu.auction.client;
import java.math.BigDecimal;
import java.time.Instant;
public record AuctionDto(Long id, Long itemId, Long sellerId, BigDecimal startingPrice,
 BigDecimal currentPrice, BigDecimal minIncrement, Instant startTime, Instant endTime,
 String status, Long leadingBidderId) {}
