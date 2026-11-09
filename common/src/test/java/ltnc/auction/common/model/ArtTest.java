package ltnc.auction.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.Year;
import org.junit.jupiter.api.Test;

class ArtTest {

    private static final Instant STORED_CREATED_AT = Instant.parse("2020-01-01T00:00:00Z");
    private static final Instant STORED_UPDATED_AT = Instant.parse("2020-06-01T00:00:00Z");

    // Keep in sync with Art.MIN_YEAR.
    private static final int MIN_YEAR = 1;

    private static Art artWithYear(int year) {
        return new Art(null, null, null, "Mona Lisa", "Portrait", 5L, "Leonardo", year);
    }

    private static Art storedArt() {
        return new Art(
                10L, STORED_CREATED_AT, STORED_UPDATED_AT, "Mona Lisa", "Portrait", 5L, "Leonardo", 1503);
    }

    // ---------- creating ----------

    @Test
    void create_keepsAllValues() {
        Art art = artWithYear(1503);

        assertEquals("Mona Lisa", art.getName());
        assertEquals("Leonardo", art.getArtist());
        assertEquals(1503, art.getYear());
        assertEquals(5L, art.getSellerId());
    }

    @Test
    void create_trimsArtist() {
        Art art = new Art(null, null, null, "Mona Lisa", null, 5L, "  Leonardo ", 1503);

        assertEquals("Leonardo", art.getArtist());
    }

    @Test
    void create_acceptsYearBoundaries() {
        assertEquals(MIN_YEAR, artWithYear(MIN_YEAR).getYear());
        int currentYear = Year.now().getValue();
        assertEquals(currentYear, artWithYear(currentYear).getYear());
    }

    @Test
    void create_rejectsYearOutsideBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> artWithYear(MIN_YEAR - 1));
        assertThrows(IllegalArgumentException.class, () -> artWithYear(-500));
        int nextYear = Year.now().getValue() + 1;
        assertThrows(IllegalArgumentException.class, () -> artWithYear(nextYear));
    }

    @Test
    void create_rejectsBlankArtist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Art(null, null, null, "Mona Lisa", null, 5L, null, 1503));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Art(null, null, null, "Mona Lisa", null, 5L, "   ", 1503));
    }

    @Test
    void create_rejectsTooLongArtist() {
        // Keep in sync with Art.MAX_ARTIST_LENGTH.
        String atLimit = "a".repeat(150);
        String aboveLimit = "a".repeat(151);

        new Art(null, null, null, "Mona Lisa", null, 5L, atLimit, 1503);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Art(null, null, null, "Mona Lisa", null, 5L, aboveLimit, 1503));
    }

    // ---------- restoring from the database ----------

    @Test
    void restore_keepsStoredIdAndTimes() {
        Art art = storedArt();

        assertEquals(10L, art.getId());
        assertEquals(STORED_CREATED_AT, art.getCreatedAt());
        assertEquals(STORED_UPDATED_AT, art.getUpdatedAt());
    }

    // ---------- updating ----------

    @Test
    void updateDetails_changesValuesAndRefreshesUpdatedAt() {
        Art art = storedArt();

        art.updateDetails("Van Gogh", 1889);

        assertEquals("Van Gogh", art.getArtist());
        assertEquals(1889, art.getYear());
        assertTrue(art.getUpdatedAt().isAfter(STORED_UPDATED_AT));
    }

    @Test
    void updateDetails_withInvalidYearChangesNothing() {
        Art art = storedArt();

        assertThrows(IllegalArgumentException.class, () -> art.updateDetails("Van Gogh", 0));

        assertEquals("Leonardo", art.getArtist());
        assertEquals(1503, art.getYear());
        assertEquals(STORED_UPDATED_AT, art.getUpdatedAt());
    }

    @Test
    void updateDetails_withInvalidArtistChangesNothing() {
        Art art = storedArt();

        assertThrows(IllegalArgumentException.class, () -> art.updateDetails(" ", 1889));

        assertEquals("Leonardo", art.getArtist());
        assertEquals(1503, art.getYear());
        assertEquals(STORED_UPDATED_AT, art.getUpdatedAt());
    }

    // ---------- category and info ----------

    @Test
    void getCategory_isArt() {
        assertEquals(ItemType.ART, artWithYear(1503).getCategory());
    }

    @Test
    void getInfo_containsNameArtistAndYear() {
        assertEquals("[ART] Mona Lisa - Artist: Leonardo (1503)", artWithYear(1503).getInfo());
    }
}