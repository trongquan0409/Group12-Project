package ltnc.auction.common.model;

import java.time.Instant;
import ltnc.auction.common.util.Validators;

/**
 * Base class of every item put up for auction.
 *
 * <p>Holds the data shared by all item types (name, description, owner). Type-specific data lives
 * in the subclasses, which also define their own category and display text.
 */
public abstract class Item extends Entity {

    // Keep in sync with the items table (name VARCHAR(150)).
    public static final int MAX_NAME_LENGTH = 150;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    private String name;
    private String description;
    private final long sellerId;

    /**
     * Creates an item.
     *
     * <p>Pass {@code null} for {@code id}, {@code createdAt} and {@code updatedAt} to create a new
     * item. Pass the stored values to restore an item loaded from the database; they are kept as is,
     * so this constructor never changes {@code updatedAt}.
     *
     * @param id the database id, or {@code null} for a new item
     * @param createdAt the creation time, or {@code null} for a new item
     * @param updatedAt the last update time, or {@code null} for a new item
     * @param name the item name, must not be blank
     * @param description the item description, may be {@code null}
     * @param sellerId the id of the seller who owns this item, must be positive
     * @throws IllegalArgumentException if {@code name}, {@code description} or {@code sellerId} is
     *     invalid
     */
    protected Item(
            Long id,
            Instant createdAt,
            Instant updatedAt,
            String name,
            String description,
            long sellerId) {
        super(id, createdAt, updatedAt);
        this.sellerId = Validators.requirePositive(sellerId, "sellerId");
        this.name = validName(name);
        this.description = validDescription(description);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public long getSellerId() {
        return sellerId;
    }

    /**
     * Changes the name and description and refreshes the update time.
     *
     * <p>Both values are validated before either is assigned, so a failed call leaves this item
     * unchanged. This method is final because the constructor relies on the same rules.
     *
     * @param name the new name, must not be blank
     * @param description the new description, may be {@code null}
     * @throws IllegalArgumentException if {@code name} or {@code description} is invalid
     */
    public final void updateBasicInfo(String name, String description) {
        String newName = validName(name);
        String newDescription = validDescription(description);
        this.name = newName;
        this.description = newDescription;
        touch();
    }

    /** Returns the category of this item. */
    public abstract ItemType getCategory();

    /** Returns a one-line, human-readable description including the type-specific data. */
    public abstract String getInfo();

    private static String validName(String name) {
        String notBlank = Validators.requireNotBlank(name, "name");
        return Validators.requireMaxLength(notBlank, MAX_NAME_LENGTH, "name");
    }

    private static String validDescription(String description) {
        String trimmed = Validators.trimToEmpty(description);
        return Validators.requireMaxLength(trimmed, MAX_DESCRIPTION_LENGTH, "description");
    }
}