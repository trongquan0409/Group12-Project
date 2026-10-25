package ltnc.auction.common.model;
import java.math.BigDecimal;
public final class Vehicle extends Item { private static final long serialVersionUID=1L; public Vehicle(String id,String name,String description,String sellerId,BigDecimal reservePrice){super(id,name,description,sellerId,reservePrice);} @Override public String getCategory(){return "VEHICLE";} }
