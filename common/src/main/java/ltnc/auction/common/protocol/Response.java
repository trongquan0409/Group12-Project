package ltnc.auction.common.protocol;
import java.io.Serializable; import java.util.List; import ltnc.auction.common.model.Auction;
public final class Response implements Serializable { private static final long serialVersionUID=1L; private final boolean success; private final String message; private final List<Auction> auctions;
 public Response(boolean success,String message,List<Auction> auctions){this.success=success;this.message=message;this.auctions=auctions==null?List.of():List.copyOf(auctions);} public boolean isSuccess(){return success;} public String getMessage(){return message;} public List<Auction> getAuctions(){return auctions;}
}
