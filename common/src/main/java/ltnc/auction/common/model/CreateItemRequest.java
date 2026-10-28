package ltnc.auction.common.model;

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
) {}
