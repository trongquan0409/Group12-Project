package vn.edu.auction.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private UserRole role;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected UserAccount() {}
    public UserAccount(String username, String passwordHash, String displayName, UserRole role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
}
