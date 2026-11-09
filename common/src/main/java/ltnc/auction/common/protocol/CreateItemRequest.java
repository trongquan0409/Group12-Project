package ltnc.auction.common.protocol;

import java.io.Serial;
import java.io.Serializable;

import ltnc.auction.common.model.ItemType;

/**
 * Data sent by a client to create an item. Only the fields that belong to {@code type} are used.
 * The seller is taken from the authenticated user on the server, never from this request.
 *
 * @param name           the item name
 * @param description    the item description, may be {@code null}
 * @param type           the kind of item to create
 * @param brand          electronics only
 * @param warrantyMonths electronics only
 * @param artist         art only
 * @param year           art only
 * @param mileageKm      vehicle only
 * @param plateNo        vehicle only
 */
public record CreateItemRequest(
        String name,
        String description,
        ItemType type,
        String brand,
        Integer warrantyMonths,
        String artist,
        Integer year,
        Integer mileageKm,
        String plateNo) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}