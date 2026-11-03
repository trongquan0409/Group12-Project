package ltnc.auction.server;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Auction;

class AuctionDomainTest {
    @Test
    void rejectsInvalidTimeRange() {
        var item = new Art(
                1L, Instant.now(), Instant.now(),
                "Painting", "desc", 1L, "Sample artist", 2020
        );
        assertThrows(IllegalArgumentException.class, () -> new Auction("a", item, BigDecimal.ONE, BigDecimal.ONE, Instant.now(), Instant.now().minusSeconds(1)));
    }
}
