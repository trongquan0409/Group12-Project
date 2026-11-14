package ltnc.auction.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class AuctionWinnerTest {
    private static final Instant START = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant END = START.plus(Duration.ofHours(1));

    private static Auction openAuction() {
        var item = new Art(1L, START, START, "Painting", "desc", 1L, "Sample artist", 2020);
        return new Auction(
                "a1", item, new BigDecimal("100000"), new BigDecimal("10000"), START, END);
    }

    private static Auction runningAuction() {
        Auction auction = openAuction();
        auction.start();
        return auction;
    }

    private static Bid bid(String bidderId, String amount) {
        return new Bid(
                "bid-" + bidderId, "a1", bidderId, new BigDecimal(amount), START.plusSeconds(1));
    }

    @Test
    void openAuctionHasNoWinner() {
        assertNull(openAuction().getWinnerId());
    }

    @Test
    void runningAuctionHasNoWinnerYetEvenWithBids() {
        Auction auction = runningAuction();
        auction.addBid(bid("bidder1", "110000"));

        assertEquals("bidder1", auction.getHighestBidderId());
        assertNull(auction.getWinnerId());
    }

    @Test
    void finishedAuctionWithoutBidsHasNoWinner() {
        Auction auction = runningAuction();
        auction.finish();

        assertNull(auction.getWinnerId());
    }

    @Test
    void finishedAuctionIsWonByTheHighestBidder() {
        Auction auction = runningAuction();
        auction.addBid(bid("bidder1", "110000"));
        auction.addBid(bid("bidder2", "120000"));
        auction.finish();

        assertEquals("bidder2", auction.getWinnerId());
    }

    @Test
    void paidAuctionKeepsItsWinner() {
        Auction auction = runningAuction();
        auction.addBid(bid("bidder1", "110000"));
        auction.finish();
        auction.markPaid();

        assertEquals("bidder1", auction.getWinnerId());
    }

    @Test
    void canceledAuctionHasNoWinner() {
        Auction auction = runningAuction();
        auction.cancel();

        assertNull(auction.getWinnerId());
    }

    @Test
    void winnerIsStillKnownAfterTheAuctionIsSavedAndLoaded() throws Exception {
        Auction auction = runningAuction();
        auction.addBid(bid("bidder1", "110000"));
        auction.finish();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(auction);
        }
        Auction loaded;
        try (ObjectInputStream in =
                     new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            loaded = (Auction) in.readObject();
        }

        assertEquals("bidder1", loaded.getWinnerId());
    }
}