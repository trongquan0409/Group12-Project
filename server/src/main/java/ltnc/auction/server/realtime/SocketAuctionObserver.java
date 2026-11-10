package ltnc.auction.server.realtime;

import ltnc.auction.common.event.AuctionEvent;
import ltnc.auction.common.protocol.ServerEvent;
import ltnc.auction.server.observer.AuctionObserver;

public final class SocketAuctionObserver
        implements AuctionObserver {

    private final RealtimeClientRegistry registry;

    public SocketAuctionObserver(
            RealtimeClientRegistry registry
    ) {
        this.registry = registry;
    }

    @Override
    public void onAuctionEvent(AuctionEvent event) {
        if (event == null) {
            return;
        }

        registry.broadcast(
                new ServerEvent(event)
        );
    }
}