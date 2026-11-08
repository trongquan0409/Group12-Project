package ltnc.auction.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ltnc.auction.common.factory.DefaultItemFactory;
import ltnc.auction.common.model.Art;
import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.common.model.ItemType;

class ItemFactoryTest {
    @Test
    void createsArtItem() {
        var factory = new DefaultItemFactory();
        var request = new CreateItemRequest(
                "Painting", "desc", 1L, ItemType.ART,
                null, 0, "Sample artist", 2025, 0, 0
        );

        var item = factory.createItem(request);

        assertInstanceOf(Art.class, item);
        assertEquals(ItemType.ART, item.getCategory());
    }

    @Test
    void rejectsMissingCategory() {
        var factory = new DefaultItemFactory();
        var request = new CreateItemRequest(
                "Painting", "desc", 1L, null,
                null, 0, "Sample artist", 2025, 0, 0
        );

        assertThrows(IllegalArgumentException.class,
                () -> factory.createItem(request));
    }
}
