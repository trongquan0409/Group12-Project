package ltnc.auction.common.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.io.Serial;

public record Request(RequestType type, String username, String password, String auctionId, BigDecimal amount,
                      String role) implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;

 public Request(
         RequestType type,
         String username,
         String password,
         String auctionId,
         BigDecimal amount
 ) {
  this(type, username, password, auctionId, amount, null);
 }

}
