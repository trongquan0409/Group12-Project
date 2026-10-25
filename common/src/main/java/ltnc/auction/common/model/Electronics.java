package ltnc.auction.common.model;
import java.math.BigDecimal;
public final class Electronics extends Item { private static final long serialVersionUID=1L; public Electronics(String id,String name,String description,String sellerId,BigDecimal reservePrice){super(id,name,description,sellerId,reservePrice);} @Override public String getCategory(){return "ELECTRONICS";} }
