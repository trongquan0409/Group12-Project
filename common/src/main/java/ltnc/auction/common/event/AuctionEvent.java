package ltnc.auction.common.event;
import java.io.Serializable; import java.math.BigDecimal;
public record AuctionEvent(String auctionId, BigDecimal highestPrice, String bidderId, String message) implements Serializable { private static final long serialVersionUID=1L; }
