package ltnc.auction.common.model;

import java.time.Instant;

import ltnc.auction.common.util.Validators;

/**
 * An electronic device put up for auction, such as a phone or a laptop.
 */
public class Electronics extends Item {

    // Keep in sync with the column that stores the brand.
    private static final int MAX_BRAND_LENGTH = 150;

    private String brand;
    private int warrantyMonths;

    /**
     * Creates an electronics item.
     *
     * <p>Pass {@code null} for {@code id}, {@code createdAt} and {@code updatedAt} to create a new
     * item. Pass the stored values to restore an item loaded from the database.
     *
     * @param id             the database id, or {@code null} for a new item
     * @param createdAt      the creation time, or {@code null} for a new item
     * @param updatedAt      the last update time, or {@code null} for a new item
     * @param name           the item name, must not be blank
     * @param description    the item description, may be {@code null}
     * @param sellerId       the id of the seller who owns this item, must be positive
     * @param brand          the brand, must not be blank
     * @param warrantyMonths the warranty period in months, must not be negative
     * @throws IllegalArgumentException if any value is invalid
     */
    public Electronics(
            Long id,
            Instant createdAt,
            Instant updatedAt,
            String name,
            String description,
            long sellerId,
            String brand,
            int warrantyMonths) {
        super(id, createdAt, updatedAt, name, description, sellerId);
        this.brand = validBrand(brand);
        this.warrantyMonths = validWarrantyMonths(warrantyMonths);
    }

    public String getBrand() {
        return brand;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    /**
     * Changes the brand and warranty and refreshes the update time.
     *
     * <p>Both values are validated before either is assigned, so a failed call leaves this item
     * unchanged.
     *
     * @param brand          the new brand, must not be blank
     * @param warrantyMonths the new warranty period in months, must not be negative
     * @throws IllegalArgumentException if a value is invalid
     */
    public void updateDetails(String brand, int warrantyMonths) {
        String newBrand = validBrand(brand);
        int newWarrantyMonths = validWarrantyMonths(warrantyMonths);
        this.brand = newBrand;
        this.warrantyMonths = newWarrantyMonths;
        touch();
    }

    @Override
    public ItemType getCategory() {
        return ItemType.ELECTRONICS;
    }

    @Override
    public String getInfo() {
        return String.format(
                "[ELECTRONICS] %s - Brand: %s (Warranty: %d month(s))", getName(), brand, warrantyMonths);
    }

    private static String validBrand(String brand) {
        String trimmed = Validators.requireNotBlank(brand, "brand");
        return Validators.requireMaxLength(trimmed, MAX_BRAND_LENGTH, "brand");
    }

    private static int validWarrantyMonths(int warrantyMonths) {
        return Validators.requireNotNegative(warrantyMonths, "warrantyMonths");
    }
}