package ltnc.auction.common.model;

import java.time.Instant;
import java.io.Serializable;

public class Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Long id;
    private final Instant createdAt;
    private Instant updatedAt;

    protected Entity(Long id, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.createdAt = (createdAt == null) ? Instant.now() : createdAt;
        this.updatedAt = (updatedAt == null) ? this.createdAt : updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    protected void touch() {
        updatedAt = Instant.now();
    }
}
