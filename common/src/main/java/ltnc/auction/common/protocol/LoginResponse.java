package ltnc.auction.common.protocol;

import java.io.Serial;
import java.io.Serializable;

/** Result returned by the server after checking login credentials. */
public record LoginResponse(boolean success, String message, String role)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
