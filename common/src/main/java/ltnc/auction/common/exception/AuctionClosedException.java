package ltnc.auction.common.exception;
public class AuctionClosedException extends Exception { public AuctionClosedException(String message){super(message);} public AuctionClosedException(){super("Phiên đấu giá đã đóng");} }
