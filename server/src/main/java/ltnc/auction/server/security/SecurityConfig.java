package ltnc.auction.server.security;

import ltnc.auction.common.model.UserRole;
import ltnc.auction.common.protocol.RequestType;

/**
 * Role-based access rules for socket requests.
 */
public final class SecurityConfig {

    /** Only administrators may manage other users' account status. */
    public boolean canManageAccounts(UserRole role) {
        return role == UserRole.ADMIN;
    }

    public boolean isPublicRequest(RequestType requestType) {
        return requestType == RequestType.LOGIN
                || requestType == RequestType.REGISTER
                || requestType == RequestType.PING;
    }

    public boolean isAllowed(UserRole role, RequestType requestType) {
        if (role == null || requestType == null) {
            return false;
        }

        return switch (requestType) {
            case LOGIN, REGISTER, PING -> true;
            case LIST_AUCTIONS, LIST_BIDS -> true;
            case LIST_SELLER_AUCTIONS -> role == UserRole.SELLER || role == UserRole.ADMIN;
            case PLACE_BID -> role == UserRole.BIDDER;
            case CREATE_AUCTION -> role == UserRole.SELLER || role == UserRole.ADMIN;
        };
    }
}
