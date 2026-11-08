package ltnc.auction.common.factory;

import ltnc.auction.common.model.Art;
import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.common.model.Electronics;
import ltnc.auction.common.model.Item;
import ltnc.auction.common.model.Vehicle;

import java.time.Instant;

public class DefaultItemFactory implements ItemFactory {
    @Override
    public Item createItem(CreateItemRequest request) {
        if (request == null || request.type() == null) {
            throw new IllegalArgumentException("Item request and type are required.");
        }

        Long newId = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        return switch (request.type()) {
            case ELECTRONICS -> {
                yield new Electronics(
                        newId, createdAt, updatedAt,
                        request.name(), request.description(), request.sellerId(),
                        request.brand(), request.warrantyMonths());
            }
            case ART -> {
                yield new Art(
                        newId, createdAt, updatedAt,
                        request.name(), request.description(), request.sellerId(),
                        request.artist(), request.year());
            }
            case VEHICLE -> {
                yield new Vehicle(
                        newId, createdAt, updatedAt,
                        request.name(), request.description(), request.sellerId(),
                        request.mileageKm(), request.plateNo());
            }
            default -> throw new IllegalArgumentException();
        };

    }
}
