package ltnc.auction.common.exception;

import java.io.Serial;

public class AuthenticationException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException() {
        super("Xác thực thất bại");
    }
}
