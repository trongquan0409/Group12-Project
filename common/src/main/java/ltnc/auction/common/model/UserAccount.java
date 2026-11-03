package ltnc.auction.common.model;

import java.io.Serializable;
import java.io.Serial;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/** Account profile shared by the auction client and server. */
@SuppressWarnings("unused") // Public model API used by serialization and future account flows.
public final class UserAccount implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final Instant createdAt;
    private String displayName;
    private UserRole role;
    private UserStatus status;
    private Instant updatedAt;

    public UserAccount(Long id, String username, String passwordHash,
                       String displayName, UserRole role, UserStatus status,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = requireText(username, "username", 50).toLowerCase(Locale.ROOT);
        this.passwordHash = requireText(passwordHash, "passwordHash", 255);
        this.displayName = requireText(displayName, "displayName", 100);
        this.role = Objects.requireNonNull(role, "role");
        this.status = Objects.requireNonNull(status, "status");
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public boolean isActive() { return status == UserStatus.ACTIVE; }

    public void updateProfile(String displayName, UserRole role, UserStatus status) {
        this.displayName = requireText(displayName, "displayName", 100);
        this.role = Objects.requireNonNull(role, "role");
        this.status = Objects.requireNonNull(status, "status");
        this.updatedAt = Instant.now();
    }

    private static String requireText(String value, String field, int maxLength) {
        String result = Objects.requireNonNull(value, field).trim();
        if (result.isEmpty() || result.length() > maxLength) {
            throw new IllegalArgumentException(field + " must contain 1 to " + maxLength + " characters");
        }
        return result;
    }
}
