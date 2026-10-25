package ltnc.auction.common.model;
import java.io.Serializable;
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id; private final String username; private final String passwordHash;
    protected User(String id, String username, String passwordHash) { this.id=id; this.username=username; this.passwordHash=passwordHash; }
    public String getId(){return id;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;}
    public abstract String getRole();
}
