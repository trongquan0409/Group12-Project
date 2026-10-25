package ltnc.auction.common.model;
public final class Seller extends User { private static final long serialVersionUID=1L; public Seller(String id,String username,String passwordHash){super(id,username,passwordHash);} @Override public String getRole(){return "SELLER";} }
