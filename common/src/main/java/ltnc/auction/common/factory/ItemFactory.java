package ltnc.auction.common.factory;

import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.common.model.Item;

public interface ItemFactory {
    Item createItem(CreateItemRequest request);
}
