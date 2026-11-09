package ltnc.auction.common.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class VehicleTest {

    private static final Instant STORED_CREATED_AT = Instant.parse("2020-01-01T00:00:00Z");
    private static final Instant STORED_UPDATED_AT = Instant.parse("2020-06-01T00:00:00Z");

    // Keep in sync with Vehicle.MIN_MILEAGE_KM, MAX_MILEAGE_KM and MAX_PLATE_NUMBER_LENGTH.
    private static final int MIN_MILEAGE_KM = 0;
    private static final int MAX_MILEAGE_KM = 9_999_999;
    private static final int MAX_PLATE_NUMBER_LENGTH = 20;

    private static Vehicle vehicle(int mileageKm, String plateNo) {
        return new Vehicle(null, null, null, "Civic", "A car", 5L, mileageKm, plateNo);
    }

    private static Vehicle storedVehicle() {
        return new Vehicle(
                10L, STORED_CREATED_AT, STORED_UPDATED_AT, "Civic", "A car", 5L, 12_000, "30A-123.45");
    }

    // ---------- creating ----------

    @Test
    void create_keepsAllValues() {
        Vehicle v = vehicle(12_000, "30A-123.45");

        assertEquals("Civic", v.getName());
        assertEquals(12_000, v.getMileageKm());
        assertEquals("30A-123.45", v.getPlateNo());
        assertEquals(5L, v.getSellerId());
    }

    @Test
    void create_keepsLettersAndLeadingZerosOfPlate() {
        assertEquals("01B-000.12", vehicle(0, "01B-000.12").getPlateNo());
    }

    @Test
    void create_trimsPlate() {
        assertEquals("30A-123.45", vehicle(0, "  30A-123.45 ").getPlateNo());
    }

    @Test
    void create_acceptsMileageBoundaries() {
        assertEquals(MIN_MILEAGE_KM, vehicle(MIN_MILEAGE_KM, "A").getMileageKm());
        assertEquals(MAX_MILEAGE_KM, vehicle(MAX_MILEAGE_KM, "A").getMileageKm());
    }

    @Test
    void create_rejectsMileageOutsideBoundaries() {
        assertThrows(IllegalArgumentException.class, () -> vehicle(MIN_MILEAGE_KM - 1, "A"));
        assertThrows(IllegalArgumentException.class, () -> vehicle(MAX_MILEAGE_KM + 1, "A"));
    }

    @Test
    void create_rejectsBlankPlate() {
        assertThrows(IllegalArgumentException.class, () -> vehicle(0, null));
        assertThrows(IllegalArgumentException.class, () -> vehicle(0, ""));
        assertThrows(IllegalArgumentException.class, () -> vehicle(0, "   "));
    }

    @Test
    void create_acceptsPlateAtLimitAndRejectsAboveIt() {
        String atLimit = "A".repeat(MAX_PLATE_NUMBER_LENGTH);
        String aboveLimit = "A".repeat(MAX_PLATE_NUMBER_LENGTH + 1);

        assertEquals(atLimit, vehicle(0, atLimit).getPlateNo());
        assertThrows(IllegalArgumentException.class, () -> vehicle(0, aboveLimit));
    }

    // ---------- restoring from the database ----------

    @Test
    void restore_keepsStoredIdAndTimes() {
        Vehicle v = storedVehicle();

        assertEquals(10L, v.getId());
        assertEquals(STORED_CREATED_AT, v.getCreatedAt());
        assertEquals(STORED_UPDATED_AT, v.getUpdatedAt());
    }

    // ---------- updating ----------

    @Test
    void updateDetails_changesValuesAndRefreshesUpdatedAt() {
        Vehicle v = storedVehicle();

        v.updateDetails(15_000, "51F-999.99");

        assertEquals(15_000, v.getMileageKm());
        assertEquals("51F-999.99", v.getPlateNo());
        assertTrue(v.getUpdatedAt().isAfter(STORED_UPDATED_AT));
    }

    @Test
    void updateDetails_withInvalidMileageChangesNothing() {
        Vehicle v = storedVehicle();

        assertThrows(IllegalArgumentException.class, () -> v.updateDetails(-1, "51F-999.99"));

        assertEquals(12_000, v.getMileageKm());
        assertEquals("30A-123.45", v.getPlateNo());
        assertEquals(STORED_UPDATED_AT, v.getUpdatedAt());
    }

    @Test
    void updateDetails_withInvalidPlateChangesNothing() {
        Vehicle v = storedVehicle();

        assertThrows(IllegalArgumentException.class, () -> v.updateDetails(15_000, " "));

        assertEquals(12_000, v.getMileageKm());
        assertEquals("30A-123.45", v.getPlateNo());
        assertEquals(STORED_UPDATED_AT, v.getUpdatedAt());
    }

    // ---------- category and info ----------

    @Test
    void getCategory_isVehicle() {
        assertEquals(ItemType.VEHICLE, vehicle(0, "A").getCategory());
    }

    @Test
    void getInfo_containsNameMileageAndPlate() {
        assertEquals(
                "[VEHICLE] Civic - mileage: 12000km (Plate Number: 30A-123.45)",
                vehicle(12_000, "30A-123.45").getInfo());
    }
}