package vn.edu.auction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bid_transactions", indexes = @Index(name = "idx_bids_auction_created", columnList = "auction_id,created_at"))
public class BidTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "auction_id", nullable = false)
    private Long auctionId;
    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected BidTransaction() {}
    public BidTransaction(Long auctionId, Long bidderId, BigDecimal amount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.amount = amount;
        this.createdAt = Instant.now();
    }
    public Long getId() { return id; }
    public Long getAuctionId() { return auctionId; }
    public Long getBidderId() { return bidderId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
