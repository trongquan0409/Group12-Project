package ltnc.auction.common.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Electronics;
import ltnc.auction.common.model.Item;
import ltnc.auction.common.model.ItemType;
import ltnc.auction.common.model.Vehicle;
import ltnc.auction.common.protocol.CreateItemRequest;
import org.junit.jupiter.api.Test;

class ItemFactoryTest {

    private static final long SELLER_ID = 1L;

    private final ItemFactory factory = new DefaultItemFactory();

    private static CreateItemRequest electronicsRequest() {
        return new CreateItemRequest(
                "Laptop", "desc", ItemType.ELECTRONICS, "Dell", 12, null, null, null, null);
    }

    private static CreateItemRequest artRequest() {
        return new CreateItemRequest(
                "Painting", "desc", ItemType.ART, null, null, "Sample artist", 2025, null, null);
    }

    private static CreateItemRequest vehicleRequest() {
        return new CreateItemRequest(
                "Civic", "desc", ItemType.VEHICLE, null, null, null, null, 12_000, "30A-123.45");
    }

    // ---------- creating each type ----------

    @Test
    void createsElectronicsItem() {
        Item item = factory.createItem(SELLER_ID, electronicsRequest());

        Electronics electronics = assertInstanceOf(Electronics.class, item);
        assertEquals(ItemType.ELECTRONICS, item.getCategory());
        assertEquals("Dell", electronics.getBrand());
        assertEquals(12, electronics.getWarrantyMonths());
    }

    @Test
    void createsArtItem() {
        Item item = factory.createItem(SELLER_ID, artRequest());

        Art art = assertInstanceOf(Art.class, item);
        assertEquals(ItemType.ART, item.getCategory());
        assertEquals("Sample artist", art.getArtist());
        assertEquals(2025, art.getYear());
    }

    @Test
    void createsVehicleItem() {
        Item item = factory.createItem(SELLER_ID, vehicleRequest());

        Vehicle vehicle = assertInstanceOf(Vehicle.class, item);
        assertEquals(ItemType.VEHICLE, item.getCategory());
        assertEquals(12_000, vehicle.getMileageKm());
        assertEquals("30A-123.45", vehicle.getPlateNo());
    }

    @Test
    void usesTheGivenSellerIdAndCommonFields() {
        Item item = factory.createItem(42L, artRequest());

        assertEquals(42L, item.getSellerId());
        assertEquals("Painting", item.getName());
        assertEquals("desc", item.getDescription());
    }

    @Test
    void newItemHasNoIdAndHasCreationTime() {
        Item item = factory.createItem(SELLER_ID, electronicsRequest());

        assertNull(item.getId());
        assertNotNull(item.getCreatedAt());
        assertEquals(item.getCreatedAt(), item.getUpdatedAt());
    }

    @Test
    void ignoresFieldsOfOtherTypes() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Laptop", "desc", ItemType.ELECTRONICS, "Dell", 12, "Some artist", 1900, 5, "X");

        assertInstanceOf(Electronics.class, factory.createItem(SELLER_ID, request));
    }

    // ---------- missing request data ----------

    @Test
    void rejectsMissingCategory() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Painting", "desc", null, null, null, "Sample artist", 2025, null, null);

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
        assertTrue(e.getMessage().contains("type"));
    }

    @Test
    void rejectsNullRequest() {
        assertThrows(NullPointerException.class, () -> factory.createItem(SELLER_ID, null));
    }

    @Test
    void rejectsMissingWarrantyMonthsForElectronics() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Laptop", "desc", ItemType.ELECTRONICS, "Dell", null, null, null, null, null);

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
        assertTrue(e.getMessage().contains("warrantyMonths"));
    }

    @Test
    void rejectsMissingYearForArt() {
        CreateItemRequest request =
                new CreateItemRequest("Painting", "desc", ItemType.ART, null, null, "A", null, null, null);

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
        assertTrue(e.getMessage().contains("year"));
    }

    @Test
    void rejectsMissingMileageForVehicle() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Civic", "desc", ItemType.VEHICLE, null, null, null, null, null, "30A-123.45");

        IllegalArgumentException e =
                assertThrows(
                        IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
        assertTrue(e.getMessage().contains("mileageKm"));
    }

    // ---------- invalid values are reported by the item classes ----------

    @Test
    void rejectsNonPositiveSellerId() {
        assertThrows(IllegalArgumentException.class, () -> factory.createItem(0L, artRequest()));
        assertThrows(IllegalArgumentException.class, () -> factory.createItem(-1L, artRequest()));
    }

    @Test
    void rejectsBlankName() {
        CreateItemRequest request =
                new CreateItemRequest("  ", "desc", ItemType.ART, null, null, "A", 2025, null, null);

        assertThrows(IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
    }

    @Test
    void rejectsBlankBrandForElectronics() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Laptop", "desc", ItemType.ELECTRONICS, " ", 12, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
    }

    @Test
    void rejectsNegativeWarrantyForElectronics() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Laptop", "desc", ItemType.ELECTRONICS, "Dell", -1, null, null, null, null);

        assertThrows(IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
    }

    @Test
    void rejectsFutureYearForArt() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Painting", "desc", ItemType.ART, null, null, "A", 9999, null, null);

        assertThrows(IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
    }

    @Test
    void rejectsBlankPlateForVehicle() {
        CreateItemRequest request =
                new CreateItemRequest(
                        "Civic", "desc", ItemType.VEHICLE, null, null, null, null, 12_000, " ");

        assertThrows(IllegalArgumentException.class, () -> factory.createItem(SELLER_ID, request));
    }
}