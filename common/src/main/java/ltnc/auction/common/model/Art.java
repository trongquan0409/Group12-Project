package ltnc.auction.common.model;
import java.time.Instant;
import java.time.Year;

public class Art extends Item{
    private String artist;
    private int year;

    public Art(Long id, Instant createdAt, Instant updatedAt, String name, String description, long sellerId, String artist, int year) {
        super(id, createdAt, updatedAt, name, description, sellerId);
        this.updateDetails(artist, year);
    }

    public String getArtist() { return artist; }
    public int getYear() { return year; }

    public void updateDetails(String artist, int year) {
        if (artist == null || artist.isBlank()) throw new IllegalArgumentException("Illegal artist");
        this.artist = artist;
        if (year < 0 || year > Year.now().getValue()) throw new IllegalArgumentException("Illegal year");
        this.year = year;
        super.touch();
    }

    @Override
    public ItemType getCategory() { return ItemType.ART; }
    @Override
    public String getInfo() {
        return String.format("[ART] %s - Artist: %s (%d)", getName(), artist, year);
    }
}
