package ltnc.auction.server.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.AuctionStatus;
import ltnc.auction.server.persistence.AuctionStore;
import ltnc.auction.server.service.AuctionManager;

class AuctionSchedulerTest {
    // The scheduler first runs after 1 second, so allow a few ticks before giving up.
    private static final Duration TIMEOUT = Duration.ofSeconds(6);

    @TempDir
    Path temp;

    private AuctionManager manager;
    private AuctionScheduler scheduler;

    @BeforeEach
    void setUp() {
        manager = AuctionManager.getInstance();
        manager.configureStore(new AuctionStore(temp.resolve("auctions.ser")));
        scheduler = new AuctionScheduler();
    }

    @AfterEach
    void tearDown() {
        scheduler.close();
    }

    private static Auction newAuction(String id, Duration startOffset, Duration endOffset) {
        Instant now = Instant.now();
        var item = new Art(1L, now, now, "Painting", "desc", 1L, "Sample artist", 2020);
        return new Auction(
                id, item, new BigDecimal("100000"), new BigDecimal("10000"),
                now.plus(startOffset), now.plus(endOffset));
    }

    // Polls instead of sleeping for a fixed time, so a passing test returns as soon as it can.
    private static boolean waitUntil(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(50);
        }
        return condition.getAsBoolean();
    }

    @Test
    void closeWithoutStartIsSafe() {
        scheduler.close();
    }

    @Test
    void startAndCloseDoNotThrow() {
        scheduler.start(manager);
        scheduler.close();
    }

    // The auction is read directly, because asking the manager would refresh it by itself.
    @Test
    void schedulerFinishesAnExpiredAuctionWithoutAnyoneQueryingTheManager() throws Exception {
        Auction expired = newAuction("a1", Duration.ofHours(-2), Duration.ofHours(-1));
        manager.addAuction(expired);
        assertEquals(AuctionStatus.OPEN, expired.getStatus());

        scheduler.start(manager);

        assertTrue(waitUntil(() -> expired.getStatus() == AuctionStatus.FINISHED));
    }

    @Test
    void schedulerStartsAnAuctionWhoseStartTimeHasPassed() throws Exception {
        Auction running = newAuction("a1", Duration.ofSeconds(-60), Duration.ofHours(1));
        manager.addAuction(running);

        scheduler.start(manager);

        assertTrue(waitUntil(() -> running.getStatus() == AuctionStatus.RUNNING));
    }

    @Test
    void schedulerSavesTheStatusChangesItMakes() throws Exception {
        Path file = temp.resolve("saved.ser");
        manager.configureStore(new AuctionStore(file));
        manager.addAuction(newAuction("a1", Duration.ofHours(-2), Duration.ofHours(-1)));

        scheduler.start(manager);

        assertTrue(waitUntil(() -> {
            // The file may be read while it is being written, which gives an empty list.
            List<Auction> saved = new AuctionStore(file).load();
            return !saved.isEmpty() && saved.get(0).getStatus() == AuctionStatus.FINISHED;
        }));
    }

    // Saving always fails here (the parent of the data file is a regular file). The scheduler
    // must survive that: a second expired auction is still finished after the first run failed.
    @Test
    void schedulerKeepsRunningAfterAFailedRun() throws Exception {
        Path notADirectory = temp.resolve("plain-file");
        Files.writeString(notADirectory, "x");
        manager.configureStore(new AuctionStore(notADirectory.resolve("data.ser")));

        Auction first = newAuction("a1", Duration.ofHours(-2), Duration.ofHours(-1));
        assertThrows(IllegalStateException.class, () -> manager.addAuction(first));
        scheduler.start(manager);
        assertTrue(waitUntil(() -> first.getStatus() == AuctionStatus.FINISHED));

        Auction second = newAuction("a2", Duration.ofHours(-2), Duration.ofHours(-1));
        assertThrows(IllegalStateException.class, () -> manager.addAuction(second));

        assertTrue(waitUntil(() -> second.getStatus() == AuctionStatus.FINISHED));
    }
}