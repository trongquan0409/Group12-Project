package ltnc.auction.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import ltnc.auction.common.exception.AuctionClosedException;
import ltnc.auction.common.exception.InvalidBidException;
import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.Bid;
import ltnc.auction.server.service.AuctionManager;

class AuctionConcurrencyTest {

    @Test
    void handlesConcurrentBidsSafely() throws Exception {
        AuctionManager manager = AuctionManager.getInstance();

        String auctionId = "concurrent-"
                + UUID.randomUUID();

        Auction auction = new Auction(
                auctionId,
                new Art(
                        1L,
                        Instant.now(),
                        Instant.now(),
                        "Concurrent Painting",
                        "Test auction",
                        1L,
                        "Test Artist",
                        2026
                ),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(60)
        );

        manager.addAuction(auction);

        int bidderCount = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(bidderCount);

        CountDownLatch ready =
                new CountDownLatch(bidderCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Bid>> futures =
                new ArrayList<>();

        try {
            for (int i = 1; i <= bidderCount; i++) {
                int bidderNumber = i;

                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    BigDecimal amount =
                            BigDecimal.valueOf(
                                    100L + bidderNumber * 10L
                            );

                    return manager.placeBid(
                            auctionId,
                            "bidder-" + bidderNumber,
                            amount
                    );
                }));
            }

            ready.await();

            start.countDown();

            int successfulBids = 0;

            for (Future<Bid> future : futures) {
                try {
                    future.get();
                    successfulBids++;
                } catch (Exception ignored) {
                    // Some concurrent bids can become invalid
                    // after another bid changes the highest price.
                }
            }

            Auction result =
                    manager.getAuction(auctionId);

            assertTrue(successfulBids > 0);

            assertEquals(
                    result.getHighestPrice(),
                    result.getBids()
                            .getLast()
                            .amount()
            );

        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void rejectsConcurrentDuplicateMinimumBids() throws Exception {
        AuctionManager manager = AuctionManager.getInstance();

        String auctionId = "duplicate-"
                + UUID.randomUUID();

        Auction auction = new Auction(
                auctionId,
                new Art(
                        1L,
                        Instant.now(),
                        Instant.now(),
                        "Duplicate Test",
                        "Test auction",
                        1L,
                        "Test Artist",
                        2026
                ),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(10),
                Instant.now().minusSeconds(5),
                Instant.now().plusSeconds(60)
        );

        manager.addAuction(auction);

        int threadCount = 10;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch ready =
                new CountDownLatch(threadCount);

        CountDownLatch start =
                new CountDownLatch(1);

        List<Future<Boolean>> futures =
                new ArrayList<>();

        try {
            for (int i = 1; i <= threadCount; i++) {
                int bidderNumber = i;

                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();

                    try {
                        manager.placeBid(
                                auctionId,
                                "bidder-" + bidderNumber,
                                BigDecimal.valueOf(110)
                        );

                        return true;

                    } catch (InvalidBidException
                             | AuctionClosedException exception) {

                        return false;
                    }
                }));
            }

            ready.await();

            start.countDown();

            int successfulBids = 0;

            for (Future<Boolean> future : futures) {
                if (future.get()) {
                    successfulBids++;
                }
            }

            Auction result =
                    manager.getAuction(auctionId);

            assertEquals(1, successfulBids);

            assertEquals(
                    BigDecimal.valueOf(110),
                    result.getHighestPrice()
            );

            assertEquals(
                    1,
                    result.getBids().size()
            );

        } finally {
            executor.shutdownNow();
        }
    }
}