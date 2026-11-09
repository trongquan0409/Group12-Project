package ltnc.auction.common.factory;

import ltnc.auction.common.model.Item;
import ltnc.auction.common.protocol.CreateItemRequest;

/**
 * Creates new {@link Item} objects from client requests.
 */
public interface ItemFactory {

    /**
     * Creates a new item of the type given in the request.
     *
     * @param sellerId the id of the authenticated seller who will own the item
     * @param request  the data sent by the client
     * @return a new item that has no id yet
     * @throws NullPointerException     if {@code request} or its type is {@code null}
     * @throws IllegalArgumentException if a field needed by the item type is missing or invalid
     */
    Item createItem(long sellerId, CreateItemRequest request);
}