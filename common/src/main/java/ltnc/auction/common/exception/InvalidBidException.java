package ltnc.auction.common.exception;
public class InvalidBidException extends Exception { public InvalidBidException(String message){super(message);} public InvalidBidException(){super("Giá đặt không hợp lệ");} }
