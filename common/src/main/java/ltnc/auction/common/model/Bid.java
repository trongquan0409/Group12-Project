package ltnc.auction.common.model;
import java.io.Serializable; import java.math.BigDecimal; import java.time.Instant;
public final class Bid implements Serializable { private static final long serialVersionUID=1L; private final String id, auctionId, bidderId; private final BigDecimal amount; private final Instant createdAt;
 public Bid(String id,String auctionId,String bidderId,BigDecimal amount,Instant createdAt){this.id=id;this.auctionId=auctionId;this.bidderId=bidderId;this.amount=amount;this.createdAt=createdAt;}
 public String getId(){return id;} public String getAuctionId(){return auctionId;} public String getBidderId(){return bidderId;} public BigDecimal getAmount(){return amount;} public Instant getCreatedAt(){return createdAt;}
}
