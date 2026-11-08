package ltnc.auction.common.protocol;

import ltnc.auction.common.model.ItemType;

public record CreateItemRequest(
        String name,
        String description,
        long sellerId,
        ItemType type,

        String brand,
        int warrantyMonths,
        String artist,
        int year,
        int mileageKm,
        String plateNo
) {
}
