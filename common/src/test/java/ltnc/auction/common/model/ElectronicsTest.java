package ltnc.auction.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Tests {@link Electronics}, including the behaviour it inherits from {@link Item}. */
class ElectronicsTest {

    private static final Instant STORED_CREATED_AT = Instant.parse("2020-01-01T00:00:00Z");
    private static final Instant STORED_UPDATED_AT = Instant.parse("2020-06-01T00:00:00Z");

    private static Electronics newItem() {
        return new Electronics(null, null, null, "Laptop", "A laptop", 5L, "Dell", 12);
    }

    private static Electronics storedItem() {
        return new Electronics(
                10L, STORED_CREATED_AT, STORED_UPDATED_AT, "Laptop", "A laptop", 5L, "Dell", 12);
    }

    // ---------- creating ----------

    @Test
    void create_keepsAllValues() {
        Electronics e = newItem();

        assertEquals("Laptop", e.getName());
        assertEquals("A laptop", e.getDescription());
        assertEquals(5L, e.getSellerId());
        assertEquals("Dell", e.getBrand());
        assertEquals(12, e.getWarrantyMonths());
    }

    @Test
    void create_newItemHasNoIdAndSetsTimes() {
        Electronics e = newItem();

        assertNull(e.getId());
        assertNotNull(e.getCreatedAt());
        assertEquals(e.getCreatedAt(), e.getUpdatedAt());
    }

    @Test
    void create_trimsTextAndTurnsNullDescriptionIntoEmpty() {
        Electronics e = new Electronics(null, null, null, "  Laptop  ", null, 5L, "  Dell ", 0);

        assertEquals("Laptop", e.getName());
        assertEquals("", e.getDescription());
        assertEquals("Dell", e.getBrand());
    }

    @Test
    void create_acceptsZeroWarranty() {
        Electronics e = new Electronics(null, null, null, "Laptop", null, 5L, "Dell", 0);

        assertEquals(0, e.getWarrantyMonths());
    }

    @Test
    void create_usesTheWarrantyArgument() {
        Electronics e = new Electronics(null, null, null, "Laptop", null, 5L, "Dell", 36);

        assertEquals(36, e.getWarrantyMonths());
    }

    // ---------- restoring from the database ----------

    @Test
    void restore_keepsStoredIdAndTimes() {
        Electronics e = storedItem();

        assertEquals(10L, e.getId());
        assertEquals(STORED_CREATED_AT, e.getCreatedAt());
        assertEquals(STORED_UPDATED_AT, e.getUpdatedAt());
    }

    // ---------- invalid values ----------

    @Test
    void create_rejectsInvalidSellerId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", null, 0L, "Dell", 12));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", null, -1L, "Dell", 12));
    }

    @Test
    void create_rejectsBlankName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, null, null, 5L, "Dell", 12));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "   ", null, 5L, "Dell", 12));
    }

    @Test
    void create_acceptsNameAtLimitAndRejectsAboveIt() {
        String atLimit = "a".repeat(Item.MAX_NAME_LENGTH);
        String aboveLimit = "a".repeat(Item.MAX_NAME_LENGTH + 1);

        new Electronics(null, null, null, atLimit, null, 5L, "Dell", 12);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, aboveLimit, null, 5L, "Dell", 12));
    }

    @Test
    void create_acceptsDescriptionAtLimitAndRejectsAboveIt() {
        String atLimit = "a".repeat(Item.MAX_DESCRIPTION_LENGTH);
        String aboveLimit = "a".repeat(Item.MAX_DESCRIPTION_LENGTH + 1);

        new Electronics(null, null, null, "Laptop", atLimit, 5L, "Dell", 12);
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", aboveLimit, 5L, "Dell", 12));
    }

    @Test
    void create_rejectsBlankBrand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", null, 5L, null, 12));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", null, 5L, "  ", 12));
    }

    @Test
    void create_rejectsTooLongBrand() {
        // Keep in sync with Electronics.MAX_BRAND_LENGTH.
        String aboveLimit = "a".repeat(151);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", null, 5L, aboveLimit, 12));
    }

    @Test
    void create_rejectsNegativeWarranty() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Electronics(null, null, null, "Laptop", null, 5L, "Dell", -1));
    }

    // ---------- updating ----------

    @Test
    void updateDetails_changesValuesAndRefreshesUpdatedAt() {
        Electronics e = storedItem();

        e.updateDetails("HP", 24);

        assertEquals("HP", e.getBrand());
        assertEquals(24, e.getWarrantyMonths());
        assertTrue(e.getUpdatedAt().isAfter(STORED_UPDATED_AT));
        assertEquals(STORED_CREATED_AT, e.getCreatedAt());
    }

    @Test
    void updateDetails_withInvalidWarrantyChangesNothing() {
        Electronics e = storedItem();

        assertThrows(IllegalArgumentException.class, () -> e.updateDetails("HP", -1));

        assertEquals("Dell", e.getBrand());
        assertEquals(12, e.getWarrantyMonths());
        assertEquals(STORED_UPDATED_AT, e.getUpdatedAt());
    }

    @Test
    void updateDetails_withInvalidBrandChangesNothing() {
        Electronics e = storedItem();

        assertThrows(IllegalArgumentException.class, () -> e.updateDetails(" ", 24));

        assertEquals("Dell", e.getBrand());
        assertEquals(12, e.getWarrantyMonths());
        assertEquals(STORED_UPDATED_AT, e.getUpdatedAt());
    }

    @Test
    void updateBasicInfo_changesValuesAndRefreshesUpdatedAt() {
        Electronics e = storedItem();

        e.updateBasicInfo("  New name ", null);

        assertEquals("New name", e.getName());
        assertEquals("", e.getDescription());
        assertTrue(e.getUpdatedAt().isAfter(STORED_UPDATED_AT));
    }

    @Test
    void updateBasicInfo_withInvalidDescriptionChangesNothing() {
        Electronics e = storedItem();
        String tooLong = "a".repeat(Item.MAX_DESCRIPTION_LENGTH + 1);

        assertThrows(IllegalArgumentException.class, () -> e.updateBasicInfo("New name", tooLong));

        assertEquals("Laptop", e.getName());
        assertEquals("A laptop", e.getDescription());
        assertEquals(STORED_UPDATED_AT, e.getUpdatedAt());
    }

    // ---------- category and info ----------

    @Test
    void getCategory_isElectronics() {
        assertEquals(ItemType.ELECTRONICS, newItem().getCategory());
    }

    @Test
    void getInfo_containsNameBrandAndWarranty() {
        assertEquals(
                "[ELECTRONICS] Laptop - Brand: Dell (Warranty: 12 month(s))", newItem().getInfo());
    }
}