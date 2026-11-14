package ltnc.auction.common.protocol;

import java.io.Serial;
import java.io.Serializable;

import ltnc.auction.common.event.AuctionEvent;

public final class ServerEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AuctionEvent auctionEvent;

    public ServerEvent(AuctionEvent auctionEvent) {
        this.auctionEvent = auctionEvent;
    }

    public AuctionEvent getAuctionEvent() {
        return auctionEvent;
    }
}