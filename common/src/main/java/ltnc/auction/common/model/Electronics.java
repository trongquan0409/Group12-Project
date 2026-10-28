package ltnc.auction.common.model;
import java.time.Instant;

public class Electronics extends Item{
    private String brand;
    private int warranty;

    public Electronics(Long id, Instant createdAt, Instant updatedAt, String name, String description, long sellerId, String brand, int warranty) {
        super(id, createdAt, updatedAt, name, description, sellerId);
        this.updateDetails(brand, warranty);
    }

    public String getBrand() { return brand; }
    public int getWarranty() { return warranty; }

    public void updateDetails(String brand, int warranty) {
        if (brand == null || brand.isBlank()) throw new IllegalArgumentException("Illegal brand");
        this.brand = brand;
        if (warranty < 0) throw new IllegalArgumentException("Illegal warranty");
        this.warranty = warranty;
        super.touch();
    }

    @Override
    public ItemType getCategory() { return ItemType.ELECTRONICS; }
    @Override
    public String getInfo() {
        return String.format("[ELECTRONICS] %s - Brand: %s (Warranty: %d month(s))", getName(), brand, warranty);
    }
}
