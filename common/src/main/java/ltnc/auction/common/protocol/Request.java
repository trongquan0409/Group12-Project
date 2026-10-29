package ltnc.auction.common.protocol;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public final class Request implements Serializable {
 private static final long serialVersionUID = 2L;

 private final RequestType type;
 private final String username;
 private final String password;
 private final String auctionId;
 private final BigDecimal amount;
 private final String role;
 private final String sessionToken;

 public Request(RequestType type) {
  this(type, null, null, null, null, null, null);
 }

 public Request(RequestType type, String username, String password) {
  this(type, username, password, null, null, null, null);
 }

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
  this(type, username, password, auctionId, amount, role, null);
 }

 public Request(
         RequestType type,
         String username,
         String password,
         String auctionId,
         BigDecimal amount,
         String role,
         String sessionToken
 ) {
  this.type = Objects.requireNonNull(type, "type must not be null");
  this.username = username;
  this.password = password;
  this.auctionId = auctionId;
  this.amount = amount;
  this.role = role;
  this.sessionToken = sessionToken;
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

 public String getSessionToken() {
  return sessionToken;
 }
}
