package ltnc.auction.client.model;
import java.math.BigDecimal; import ltnc.auction.common.model.Auction;
public record AuctionRow(String id,String itemName,String status,BigDecimal highestPrice,Auction source){@Override public String toString(){return itemName+" | "+status+" | Giá cao nhất: "+highestPrice;}}
