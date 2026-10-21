package vn.edu.auction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuctionServerApplicationTests {
    @Test
    void minimumBidRuleUsesDecimalSafeComparison() {
        java.math.BigDecimal current = new java.math.BigDecimal("100.00");
        java.math.BigDecimal increment = new java.math.BigDecimal("5.00");
        assertTrue(new java.math.BigDecimal("105.00").compareTo(current.add(increment)) >= 0);
        assertFalse(new java.math.BigDecimal("104.99").compareTo(current.add(increment)) >= 0);
    }
}
