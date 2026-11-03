package ltnc.auction.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Auction;
import ltnc.auction.server.persistence.AuctionStore;

class AuctionStoreTest {
    @TempDir
    Path temp;

    @Test
    void savesAndLoadsAuction() {
        var store = new AuctionStore(temp.resolve("data.ser"));
        var item = new Art(
                1L, Instant.now(), Instant.now(),
                "Painting", "desc", 1L, "Sample artist", 2020
        );
        var a = new Auction("a", item, BigDecimal.TEN, BigDecimal.ONE, Instant.now().minusSeconds(1), Instant.now().plusSeconds(60));
        store.save(List.of(a));
        var loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals("a", loaded.get(0).getId());
    }
}
