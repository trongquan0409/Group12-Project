package ltnc.auction.common.model;
import java.time.Instant;

public abstract class Item extends Entity {
 private String name;
 private String description;
 private final long sellerId;

 protected Item(Long id, Instant createAt, Instant updateAt, String name, String description, long sellerId) {
  super(id, createAt, updateAt);
  if (sellerId <= 0) throw new IllegalArgumentException("Invalid sellerId");
  this.sellerId = sellerId;
  this.updateBasicInfo(name, description);
 }

 public String getName() { return name; }
 public String getDescription() { return description; }
 public long getSellerId() { return sellerId; }

 public final void updateBasicInfo(String name, String description) {
  if (name == null || name.isBlank()) throw new IllegalArgumentException("Invalid name");
  this.name = name.trim();
  this.description = (description == null) ? "" : description.trim();
  super.touch();
 }

 public abstract ItemType getCategory();
 public abstract String getInfo();
}
