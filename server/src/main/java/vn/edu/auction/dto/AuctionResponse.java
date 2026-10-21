package vn.edu.auction.dto;
import vn.edu.auction.model.Auction;
import vn.edu.auction.model.AuctionStatus;
import java.math.BigDecimal;
import java.time.Instant;
public record AuctionResponse(Long id, Long itemId, Long sellerId, BigDecimal startingPrice,
  BigDecimal currentPrice, BigDecimal minIncrement, Instant startTime, Instant endTime,
  AuctionStatus status, Long leadingBidderId) {
    public static AuctionResponse from(Auction a) {
        return new AuctionResponse(a.getId(), a.getItemId(), a.getSellerId(), a.getStartingPrice(),
          a.getCurrentPrice(), a.getMinIncrement(), a.getStartTime(), a.getEndTime(),
          a.getStatus(), a.getLeadingBidderId());
    }
}
