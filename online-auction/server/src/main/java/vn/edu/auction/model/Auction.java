package vn.edu.auction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "auctions", indexes = @Index(name = "idx_auctions_status_time", columnList = "status,start_time,end_time"))
public class Auction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "item_id", nullable = false, unique = true)
    private Long itemId;
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    @Column(name = "starting_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;
    @Column(name = "current_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentPrice;
    @Column(name = "min_increment", nullable = false, precision = 15, scale = 2)
    private BigDecimal minIncrement;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AuctionStatus status;
    @Column(name = "leading_bidder_id")
    private Long leadingBidderId;
    @Version
    @Column(nullable = false)
    private long version;

    protected Auction() {}
    public Auction(Long itemId, Long sellerId, BigDecimal startingPrice, BigDecimal minIncrement,
                   Instant startTime, Instant endTime) {
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.minIncrement = minIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = Instant.now().isBefore(startTime) ? AuctionStatus.OPEN : AuctionStatus.RUNNING;
    }
    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public Long getSellerId() { return sellerId; }
    public BigDecimal getStartingPrice() { return startingPrice; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getMinIncrement() { return minIncrement; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public AuctionStatus getStatus() { return status; }
    public Long getLeadingBidderId() { return leadingBidderId; }
    public long getVersion() { return version; }
    public void applyBid(BigDecimal amount, Long bidderId) {
        this.currentPrice = amount;
        this.leadingBidderId = bidderId;
    }
    public void setStatus(AuctionStatus status) { this.status = status; }
}
