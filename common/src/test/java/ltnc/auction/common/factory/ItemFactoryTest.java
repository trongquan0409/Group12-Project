package ltnc.auction.common.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ltnc.auction.common.model.Art;
import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.common.model.ItemType;

class ItemFactoryTest {
    @Test
    void createsArtItem() {
        var factory = new DefaultItemFactory();
        var request = new CreateItemRequest(
                "Painting", "desc", ItemType.ART,
                null, null, "Sample artist", 2025, null, null
        );

        var item = factory.createItem(1L, request);

        assertInstanceOf(Art.class, item);
        assertEquals(ItemType.ART, item.getCategory());
    }

    @Test
    void rejectsMissingCategory() {
        var factory = new DefaultItemFactory();
        var request = new CreateItemRequest(
                "Painting", "desc", null,
                null, null, "Sample artist", 2025, null, null
        );

        assertThrows(IllegalArgumentException.class,
                () -> factory.createItem(1L, request));
    }
}
