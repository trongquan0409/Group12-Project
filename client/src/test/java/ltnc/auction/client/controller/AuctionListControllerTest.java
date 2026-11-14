package ltnc.auction.client.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.AuctionStatus;
import ltnc.auction.common.model.Electronics;
import ltnc.auction.common.model.Vehicle;

class AuctionListControllerTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Test
    void parseEndAtCombinesDateHourAndMinute() {
        LocalDate date = LocalDate.of(2026, 11, 20);
        Instant actual = AuctionListController.parseEndAt(date, "14", "30", ZONE);
        Instant expected = LocalDateTime.of(2026, 11, 20, 14, 30)
                .atZone(ZONE)
                .toInstant();
        assertEquals(expected, actual);
    }

    @Test
    void parseEndAtAcceptsSingleDigitAndWhitespace() {
        LocalDate date = LocalDate.of(2026, 11, 20);
        Instant actual = AuctionListController.parseEndAt(date, " 9 ", " 5", ZONE);
        Instant expected = LocalDateTime.of(2026, 11, 20, 9, 5)
                .atZone(ZONE)
                .toInstant();
        assertEquals(expected, actual);
    }

    @Test
    void parseEndAtRejectsInvalidHourOrMinute() {
        LocalDate date = LocalDate.of(2026, 11, 20);
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parseEndAt(date, "24", "00", ZONE));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parseEndAt(date, "-1", "00", ZONE));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parseEndAt(date, "10", "60", ZONE));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parseEndAt(date, "abc", "15", ZONE));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parseEndAt(date, "", "15", ZONE));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parseEndAt(null, "10", "15", ZONE));
    }

    @Test
    void matchesBidderFilterMatchesByItemNameAndAuctionIdCaseInsensitive() {
        Electronics item = new Electronics(
                1L, null, null, "MacBook Pro M3", "Laptop", 10L, "Apple", 12);
        Instant start = Instant.parse("2026-11-10T10:00:00Z");
        Instant end = Instant.parse("2026-11-20T10:00:00Z");
        Auction auction = new Auction(
                "AUC-101", item, new BigDecimal("25000000"), new BigDecimal("500000"), start, end);
        auction.setStatus(AuctionStatus.RUNNING);

        assertTrue(AuctionListController.matchesBidderFilter(auction, "", "Tất cả"));
        assertTrue(AuctionListController.matchesBidderFilter(auction, "macbook", "Tất cả"));
        assertTrue(AuctionListController.matchesBidderFilter(auction, "auc-101", "Đang diễn ra"));
        assertFalse(AuctionListController.matchesBidderFilter(auction, "iphone", "Tất cả"));
    }

    @Test
    void matchesBidderFilterFiltersByVietnameseStatusLabelAndHandlesNull() {
        Electronics item = new Electronics(
                2L, null, null, "iPad Air", "Tablet", 10L, "Apple", 6);
        Instant start = Instant.parse("2026-11-10T10:00:00Z");
        Instant end = Instant.parse("2026-11-20T10:00:00Z");
        Auction auction = new Auction(
                "AUC-202", item, new BigDecimal("12000000"), new BigDecimal("200000"), start, end);
        auction.setStatus(AuctionStatus.FINISHED);

        assertTrue(AuctionListController.matchesBidderFilter(auction, null, "Đã kết thúc"));
        assertTrue(AuctionListController.matchesBidderFilter(auction, "  ", null));
        assertFalse(AuctionListController.matchesBidderFilter(auction, "", "Đang mở"));
        assertFalse(AuctionListController.matchesBidderFilter(auction, "", "Đang diễn ra"));
        assertFalse(AuctionListController.matchesBidderFilter(null, "", "Tất cả"));
    }

    @Test
    void matchesBidderFilterFiltersByCategoryAndPriceRange() {
        Instant start = Instant.parse("2026-11-10T10:00:00Z");
        Instant end = Instant.parse("2026-11-20T10:00:00Z");
        Auction electronics = new Auction(
                "AUC-1",
                new Electronics(1L, null, null, "iPhone 15", "Phone", 1L, "Apple", 12),
                new BigDecimal("20000000"),
                new BigDecimal("500000"),
                start,
                end);
        Auction art = new Auction(
                "AUC-2",
                new Art(2L, null, null, "Tranh sơn dầu", "Art", 1L, "Artist", 2020),
                new BigDecimal("8000000"),
                new BigDecimal("200000"),
                start,
                end);
        Auction vehicle = new Auction(
                "AUC-3",
                new Vehicle(3L, null, null, "SH 150i", "Xe", 1L, 5000, "29A1-12345"),
                new BigDecimal("75000000"),
                new BigDecimal("1000000"),
                start,
                end);

        assertTrue(AuctionListController.matchesBidderFilter(
                art, "", "Nghệ thuật", "Tất cả",
                new BigDecimal("5000000"), new BigDecimal("10000000")));
        assertFalse(AuctionListController.matchesBidderFilter(
                electronics, "", "Nghệ thuật", "Tất cả", null, null));
        assertFalse(AuctionListController.matchesBidderFilter(
                vehicle, "", "Tất cả", "Tất cả",
                new BigDecimal("10000000"), new BigDecimal("50000000")));
        assertTrue(AuctionListController.matchesBidderFilter(
                electronics, "", "Điện tử", "Đang mở",
                new BigDecimal("20000000"), new BigDecimal("20000000")));
    }

    @Test
    void parsePriceFilterParsesPlainAndGroupedValuesAndRejectsInvalid() {
        assertNull(AuctionListController.parsePriceFilter(null));
        assertNull(AuctionListController.parsePriceFilter("   "));
        assertEquals(new BigDecimal("5000000"), AuctionListController.parsePriceFilter("5000000"));
        assertEquals(new BigDecimal("5000000"), AuctionListController.parsePriceFilter("5.000.000"));
        assertEquals(new BigDecimal("5000000"), AuctionListController.parsePriceFilter("5,000,000"));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parsePriceFilter("-100"));
        assertThrows(IllegalArgumentException.class,
                () -> AuctionListController.parsePriceFilter("abc"));
    }

    @Test
    void bidderSortOrdersNamesWithNumbersFirstThenAlphabeticalAndReversesOnDesc() {
        Instant start = Instant.parse("2026-11-10T10:00:00Z");
        Instant end = Instant.parse("2026-11-20T10:00:00Z");
        Auction a1 = new Auction("A1", new Electronics(1L, null, null, "Bình gốm cổ", "", 1L, "X", 0),
                new BigDecimal("1000000"), new BigDecimal("10000"), start, end);
        Auction a2 = new Auction("A2", new Electronics(2L, null, null, "iPhone 15", "", 1L, "Apple", 12),
                new BigDecimal("25000000"), new BigDecimal("100000"), start, end);
        Auction a3 = new Auction("A3", new Electronics(3L, null, null, "Áo dài lụa", "", 1L, "VN", 0),
                new BigDecimal("3000000"), new BigDecimal("50000"), start, end);
        Auction a4 = new Auction("A4", new Electronics(4L, null, null, "iPhone 9", "", 1L, "Apple", 6),
                new BigDecimal("9000000"), new BigDecimal("100000"), start, end);
        Auction a5 = new Auction("A5", new Electronics(5L, null, null, "911 Porsche", "", 1L, "Porsche", 24),
                new BigDecimal("900000000"), new BigDecimal("5000000"), start, end);
        Auction a6 = new Auction("A6", new Electronics(6L, null, null, "Đàn piano", "", 1L, "Yamaha", 12),
                new BigDecimal("15000000"), new BigDecimal("200000"), start, end);

        List<Auction> input = List.of(a1, a2, a3, a4, a5, a6);

        List<String> ascNames = input.stream()
                .sorted(AuctionListController.bidderAuctionComparator(AuctionListController.SORT_NAME_ASC))
                .map(a -> a.getItem().getName())
                .toList();
        assertEquals(
                List.of("911 Porsche", "iPhone 9", "iPhone 15", "Áo dài lụa", "Bình gốm cổ", "Đàn piano"),
                ascNames);

        List<String> descNames = input.stream()
                .sorted(AuctionListController.bidderAuctionComparator(AuctionListController.SORT_NAME_DESC))
                .map(a -> a.getItem().getName())
                .toList();
        assertEquals(
                List.of("Đàn piano", "Bình gốm cổ", "Áo dài lụa", "iPhone 15", "iPhone 9", "911 Porsche"),
                descNames);

        List<String> priceAscIds = input.stream()
                .sorted(AuctionListController.bidderAuctionComparator(AuctionListController.SORT_PRICE_ASC))
                .map(Auction::getId)
                .toList();
        assertEquals(List.of("A1", "A3", "A4", "A6", "A2", "A5"), priceAscIds);

        List<String> priceDescIds = input.stream()
                .sorted(AuctionListController.bidderAuctionComparator(AuctionListController.SORT_PRICE_DESC))
                .map(Auction::getId)
                .toList();
        assertEquals(List.of("A5", "A2", "A6", "A4", "A3", "A1"), priceDescIds);
    }

    @Test
    void priceSliderHelpersComputeNiceBoundsAndSnapCleanly() {
        assertEquals(AuctionListController.DEFAULT_SLIDER_MAX_PRICE,
                AuctionListController.computeSliderMaxBound(List.of()));

        Instant start = Instant.parse("2026-11-10T10:00:00Z");
        Instant end = Instant.parse("2026-11-20T10:00:00Z");
        Auction a1 = new Auction("A1", new Electronics(1L, null, null, "Phone", "", 1L, "Apple", 12),
                new BigDecimal("22300000"), new BigDecimal("100000"), start, end);
        Auction a2 = new Auction("A2", new Vehicle(2L, null, null, "Car", "", 1L, 0, "30A-12345"),
                new BigDecimal("120000000"), new BigDecimal("1000000"), start, end);

        assertEquals(25_000_000L, AuctionListController.computeSliderMaxBound(List.of(a1)));
        assertEquals(150_000_000L, AuctionListController.computeSliderMaxBound(List.of(a1, a2)));

        assertEquals(0L, AuctionListController.snapPriceToStep(-500, 100_000_000L));
        assertEquals(25_000_000L, AuctionListController.snapPriceToStep(25_400_000, 100_000_000L));
        assertEquals(26_000_000L, AuctionListController.snapPriceToStep(25_600_000, 100_000_000L));
        assertEquals(100_000_000L, AuctionListController.snapPriceToStep(120_000_000, 100_000_000L));
    }
}
