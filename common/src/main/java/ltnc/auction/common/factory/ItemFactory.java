package ltnc.auction.common.factory;
import ltnc.auction.common.model.*;

public interface ItemFactory {
    Item createItem(CreateItemRequest request);
}
