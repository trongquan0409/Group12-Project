package ltnc.auction.common.exception;
public class AuthenticationException extends Exception { public AuthenticationException(String message){super(message);} public AuthenticationException(){super("Xác thực thất bại");} }
