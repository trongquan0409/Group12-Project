package ltnc.auction.common.model;
import java.io.Serializable;
import java.math.BigDecimal;
public abstract class Item implements Serializable {
 private static final long serialVersionUID=1L; private final String id; private final String name; private final String description; private final String sellerId; private final BigDecimal reservePrice;
 protected Item(String id,String name,String description,String sellerId,BigDecimal reservePrice){this.id=id;this.name=name;this.description=description;this.sellerId=sellerId;this.reservePrice=reservePrice;}
 public String getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public String getSellerId(){return sellerId;} public BigDecimal getReservePrice(){return reservePrice;} public abstract String getCategory();
}
