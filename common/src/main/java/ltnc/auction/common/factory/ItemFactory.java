package ltnc.auction.common.factory;
import java.math.BigDecimal; import ltnc.auction.common.model.*;
/** Factory Method: centralizes Item construction by category. */
public final class ItemFactory { private ItemFactory(){} public static Item create(String category,String id,String name,String description,String sellerId,BigDecimal reservePrice){return switch(category.toUpperCase()){case "ELECTRONICS"->new Electronics(id,name,description,sellerId,reservePrice);case "ART"->new Art(id,name,description,sellerId,reservePrice);case "VEHICLE"->new Vehicle(id,name,description,sellerId,reservePrice);default->throw new IllegalArgumentException("Danh mục sản phẩm không hợp lệ: "+category);};} }
