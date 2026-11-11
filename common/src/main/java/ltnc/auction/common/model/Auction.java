package ltnc.auction.common.model;

import ltnc.auction.common.util.Validators;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Auction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id;
    private final Item item;
    private final BigDecimal startingPrice, increment;
    private final Instant startAt, endAt;
    private AuctionStatus status;
    private BigDecimal highestPrice;
    private String highestBidderId;
    private final List<Bid> bids = new ArrayList<>();

    public Auction(
            String id,
            Item item,
            BigDecimal startingPrice,
            BigDecimal increment,
            Instant startAt,
            Instant endAt) {
        Validators.requireEndAfterStart(startAt, endAt);
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.item = Objects.requireNonNull(item, "item must not be null");
        this.startingPrice = Validators.requireNotNegative(startingPrice, "startingPrice");
        this.increment = Validators.requirePositive(increment, "increment");
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = AuctionStatus.OPEN;
        this.highestPrice = startingPrice;
    }

    public String getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public BigDecimal getIncrement() {
        return increment;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public synchronized AuctionStatus getStatus() {
        return status;
    }

    public synchronized void start() {
        if (status != AuctionStatus.OPEN) {
            throw new IllegalStateException("Only OPEN status can start, now is " + status);
        }
        status = AuctionStatus.RUNNING;
    }

    public synchronized void finish() {
        if (status != AuctionStatus.RUNNING) {
            throw new IllegalStateException("Only RUNNING status can finish, now is " + status);
        }
        status = AuctionStatus.FINISHED;
    }

    public synchronized void cancel() {
        boolean canCancel = (status == AuctionStatus.OPEN) ||
                (status == AuctionStatus.RUNNING && bids.isEmpty());
        if (!canCancel) {
            throw new IllegalStateException("Only OPEN status or RUNNING status with no bids can cancel, now is " + status);
        }
        status = AuctionStatus.CANCELED;
    }

    public synchronized void markPaid() {
        boolean canMarkPaid = (status == AuctionStatus.FINISHED && highestBidderId != null);
        if (!canMarkPaid) {
            throw new IllegalStateException("Only FINISHED status with a highest bidder can mark paid");
        }
        status = AuctionStatus.PAID;
    }

    public synchronized boolean refreshStatus(Instant now) {
        AuctionStatus beforeStatus = status;
        if (status == AuctionStatus.OPEN && !now.isBefore(startAt)) {
            start();
        }
        if (status == AuctionStatus.RUNNING && !now.isBefore(endAt)) {
            finish();
        }
        return beforeStatus != status;
    }

    public synchronized BigDecimal getHighestPrice() {
        return highestPrice;
    }

    public synchronized String getHighestBidderId() {
        return highestBidderId;
    }

    public synchronized List<Bid> getBids() {
        return List.copyOf(bids);
    }

    public synchronized void addBid(Bid bid) {
        bids.add(bid);
        highestPrice = bid.amount();
        highestBidderId = bid.bidderId();
    }
}
