package ltnc.auction.common.factory;

import java.util.Objects;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.model.Electronics;
import ltnc.auction.common.model.Item;
import ltnc.auction.common.model.Vehicle;
import ltnc.auction.common.protocol.CreateItemRequest;


/**
 * Default {@link ItemFactory} that picks the item class from the type given in the request.
 *
 * <p>This class only chooses the class and builds the object. The values of each field are
 * validated by the item classes themselves.
 */
public class DefaultItemFactory implements ItemFactory {

    /**
     * {@inheritDoc}
     *
     * <p>Only the fields that belong to the requested type are read; the others are ignored.
     */
    @Override
    public Item createItem(long sellerId, CreateItemRequest request) {
        Objects.requireNonNull(request, "request must not be null");
        if (request.type() == null) {
            throw new IllegalArgumentException("type must not be null");
        }

        return switch (request.type()) {
            case ELECTRONICS -> new Electronics(
                    null, null, null,
                    request.name(), request.description(), sellerId,
                    request.brand(), required(request.warrantyMonths(), "warrantyMonths"));
            case ART -> new Art(
                    null, null, null,
                    request.name(), request.description(), sellerId,
                    request.artist(), required(request.year(), "year"));
            case VEHICLE -> new Vehicle(
                    null, null, null,
                    request.name(), request.description(), sellerId,
                    required(request.mileageKm(), "mileageKm"),
                    request.plateNo());
        };
    }

    /**
     * Unwraps a number sent by the client.
     *
     * @throws IllegalArgumentException if the client did not send the value
     */
    private static int required(Integer value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value;
    }
}
