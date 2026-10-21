package vn.edu.auction.dto;
import java.math.BigDecimal;
import java.time.Instant;
public record AuctionEvent(String type, Long auctionId, BigDecimal currentPrice,
                           Long leadingBidderId, Instant endTime, Instant eventTime) {}
