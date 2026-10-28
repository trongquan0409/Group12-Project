package ltnc.auction.common.factory;
import ltnc.auction.common.model.*;
import java.time.Instant;

public class DefaultItemFactory implements ItemFactory{
    @Override
    public Item createItem(CreateItemRequest request) {
        Long newId = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        return switch (request.type()) {
            case ELECTRONICS -> {
                yield new Electronics(
                        newId, createdAt, updatedAt,
                        request.name(), request.description(), request.sellerId(),
                        request.brand(), request.warranty());
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
