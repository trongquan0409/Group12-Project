
package ltnc.auction.common.protocol;

import java.io.Serializable;
import java.io.Serial;
import java.util.List;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.Bid;

public record Response(boolean success, String message, List<Auction> auctions, String role,
                       List<Bid> bids) implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;

 public Response(boolean success, String message, List<Auction> auctions) {
  this(success, message, auctions, null, null);
 }

 public Response(
         boolean success,
         String message,
         List<Auction> auctions,
         String role
 ) {
  this(success, message, auctions, role, null);
 }

 public Response(
         boolean success,
         String message,
         List<Auction> auctions,
         String role,
         List<Bid> bids
 ) {
  this.success = success;
  this.message = message;
  this.auctions = auctions == null
          ? List.of()
          : List.copyOf(auctions);
  this.role = role;
  this.bids = bids == null
          ? List.of()
          : List.copyOf(bids);
 }
}
