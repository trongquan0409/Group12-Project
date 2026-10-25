package ltnc.auction.server;
import static org.junit.jupiter.api.Assertions.*; import java.math.BigDecimal; import java.time.Instant; import org.junit.jupiter.api.Test; import ltnc.auction.common.model.*;
class AuctionDomainTest {@Test void rejectsInvalidTimeRange(){var item=new Art("i","art","desc","s",BigDecimal.ONE);assertThrows(IllegalArgumentException.class,()->new Auction("a",item,BigDecimal.ONE,BigDecimal.ONE,Instant.now(),Instant.now().minusSeconds(1)));}}
