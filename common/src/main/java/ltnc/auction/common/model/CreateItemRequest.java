package ltnc.auction.common.model;

import java.io.Serial;
import java.io.Serializable;

public record CreateItemRequest(
        String name,
        String description,
        long sellerId,
        ItemType type,
        String brand,
        int warranty,
        String artist,
        int year,
        int mileageKm,
        int plateNo
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
