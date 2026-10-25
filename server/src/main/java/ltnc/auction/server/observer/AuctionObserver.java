package ltnc.auction.server.observer;
import ltnc.auction.common.event.AuctionEvent;
@FunctionalInterface public interface AuctionObserver { void onAuctionEvent(AuctionEvent event); }
