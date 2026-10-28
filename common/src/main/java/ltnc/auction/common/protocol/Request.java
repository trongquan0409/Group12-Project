package ltnc.auction.common.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.io.Serial;

public final class Request implements Serializable {
 @Serial
 private static final long serialVersionUID = 1L;

 private final RequestType type;
 private final String username;
 private final String password;
 private final String auctionId;
 private final BigDecimal amount;
 private final String role;

 public Request(
         RequestType type,
         String username,
         String password,
         String auctionId,
         BigDecimal amount
 ) {
  this(type, username, password, auctionId, amount, null);
 }

 public Request(
         RequestType type,
         String username,
         String password,
         String auctionId,
         BigDecimal amount,
         String role
 ) {
  this.type = type;
  this.username = username;
  this.password = password;
  this.auctionId = auctionId;
  this.amount = amount;
  this.role = role;
 }

 public RequestType getType() {
  return type;
 }

 public String getUsername() {
  return username;
 }

 public String getPassword() {
  return password;
 }

 public String getAuctionId() {
  return auctionId;
 }

 public BigDecimal getAmount() {
  return amount;
 }

 public String getRole() {
  return role;
 }
}
