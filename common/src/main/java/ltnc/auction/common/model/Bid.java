package ltnc.auction.common.model;
import java.io.Serial;
import java.io.Serializable; import java.math.BigDecimal; import java.time.Instant;

public record Bid(String id, String auctionId, String bidderId, BigDecimal amount,
                  Instant createdAt) implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;
}
