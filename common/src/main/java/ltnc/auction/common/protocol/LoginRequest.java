package ltnc.auction.common.protocol;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/** Credentials sent by a client when requesting login. */
public record LoginRequest(String username, String password) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public LoginRequest {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
    }

    public RequestType type() {
        return RequestType.LOGIN;
    }
}
