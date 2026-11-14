package ltnc.auction.common.model;

import java.time.Instant;
import java.time.Year;

import ltnc.auction.common.util.Validators;

/**
 * A work of art put up for auction, such as a painting or a sculpture.
 */
public class Art extends Item {

    // Keep in sync with the column that stores the artist.
    private static final int MAX_ARTIST_LENGTH = 150;

    // The system only accepts works from the Common Era onwards (a decision of the team).
    private static final int MIN_YEAR = 1;

    private String artist;
    private int year;

    /**
     * Creates an art item.
     *
     * <p>Pass {@code null} for {@code id}, {@code createdAt} and {@code updatedAt} to create a new
     * item. Pass the stored values to restore an item loaded from the database.
     *
     * @param id          the database id, or {@code null} for a new item
     * @param createdAt   the creation time, or {@code null} for a new item
     * @param updatedAt   the last update time, or {@code null} for a new item
     * @param name        the item name, must not be blank
     * @param description the item description, may be {@code null}
     * @param sellerId    the id of the seller who owns this item, must be positive
     * @param artist      the artist, must not be blank
     * @param year        the year the work was created, from {@value #MIN_YEAR} to the current year
     * @throws IllegalArgumentException if any value is invalid
     */
    public Art(
            Long id,
            Instant createdAt,
            Instant updatedAt,
            String name,
            String description,
            long sellerId,
            String artist,
            int year) {
        super(id, createdAt, updatedAt, name, description, sellerId);
        this.artist = validArtist(artist);
        this.year = validYear(year);
    }

    public String getArtist() {
        return artist;
    }

    public int getYear() {
        return year;
    }

    /**
     * Changes the artist and year and refreshes the update time.
     *
     * <p>Both values are validated before either is assigned, so a failed call leaves this item
     * unchanged.
     *
     * @param artist the new artist, must not be blank
     * @param year   the new creation year, from {@value #MIN_YEAR} to the current year
     * @throws IllegalArgumentException if a value is invalid
     */
    public void updateDetails(String artist, int year) {
        String newArtist = validArtist(artist);
        int newYear = validYear(year);
        this.artist = newArtist;
        this.year = newYear;
        touch();
    }

    @Override
    public ItemType getCategory() {
        return ItemType.ART;
    }

    @Override
    public String getInfo() {
        return String.format("[ART] %s - Artist: %s (%d)", getName(), artist, year);
    }

    private static String validArtist(String artist) {
        String trimmed = Validators.requireNotBlank(artist, "artist");
        return Validators.requireMaxLength(trimmed, MAX_ARTIST_LENGTH, "artist");
    }

    private static int validYear(int year) {
        return Validators.requireInRange(year, MIN_YEAR, Year.now().getValue(), "year");
    }
}