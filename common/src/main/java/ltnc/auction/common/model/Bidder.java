package ltnc.auction.common.model;
public final class Bidder extends User { private static final long serialVersionUID=1L; public Bidder(String id,String username,String passwordHash){super(id,username,passwordHash);} @Override public String getRole(){return "BIDDER";} }
