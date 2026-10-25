package ltnc.auction.common.protocol;
import java.io.Serializable; import java.math.BigDecimal;
public final class Request implements Serializable { private static final long serialVersionUID=1L; private final RequestType type; private final String username,password,auctionId; private final BigDecimal amount;
 public Request(RequestType type,String username,String password,String auctionId,BigDecimal amount){this.type=type;this.username=username;this.password=password;this.auctionId=auctionId;this.amount=amount;}
 public RequestType getType(){return type;} public String getUsername(){return username;} public String getPassword(){return password;} public String getAuctionId(){return auctionId;} public BigDecimal getAmount(){return amount;}
}
