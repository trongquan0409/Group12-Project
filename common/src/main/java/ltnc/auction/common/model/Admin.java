package ltnc.auction.common.model;
public final class Admin extends User { private static final long serialVersionUID=1L; public Admin(String id,String username,String passwordHash){super(id,username,passwordHash);} @Override public String getRole(){return "ADMIN";} }
