package ltnc.auction.common.protocol;

import java.io.Serial;
import java.io.Serializable;

import ltnc.auction.common.model.UserRole;

/**
 * Result returned by the server after checking login credentials.
 */
public record LoginResponse(
        boolean success,
        String message,
        String token,
        String username,
        UserRole role
)
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
