package vn.edu.auction.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "items")
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ItemCategory category = ItemCategory.OTHER;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected Item() {}
    public Item(Long sellerId, String name, String description, ItemCategory category) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.category = category == null ? ItemCategory.OTHER : category;
    }
    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemCategory getCategory() { return category; }
    public void update(String name, String description, ItemCategory category) {
        this.name = name;
        this.description = description;
        this.category = category == null ? ItemCategory.OTHER : category;
        this.updatedAt = Instant.now();
    }
}
