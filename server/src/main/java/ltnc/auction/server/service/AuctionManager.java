package ltnc.auction.server.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import ltnc.auction.common.event.AuctionEvent;
import ltnc.auction.common.exception.AuctionClosedException;
import ltnc.auction.common.exception.InvalidBidException;
import ltnc.auction.common.factory.DefaultItemFactory;
import ltnc.auction.common.factory.ItemFactory;
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.AuctionStatus;
import ltnc.auction.common.model.Bid;
import ltnc.auction.common.model.Item;
import ltnc.auction.common.protocol.CreateItemRequest;
import ltnc.auction.server.observer.AuctionObserver;
import ltnc.auction.server.persistence.AuctionStore;

public final class AuctionManager {

    private static final AuctionManager INSTANCE = new AuctionManager();

    private final Map<String, Auction> auctions =
            new LinkedHashMap<>();

    private final Map<String, ReentrantLock> locks =
            new HashMap<>();

    private final List<AuctionObserver> observers =
            new CopyOnWriteArrayList<>();

    private final ItemFactory itemFactory =
            new DefaultItemFactory();

    private AuctionStore store;

    private AuctionManager() {
    }

    public static AuctionManager getInstance() {
        return INSTANCE;
    }

    private static long sellerIdOf(String sellerUsername) {
        return Math.max(
                1L,
                (long) sellerUsername.hashCode()
                        & 0x7fffffffL
        );
    }

    public synchronized void configureStore(AuctionStore store) {
        this.store = store;

        auctions.clear();
        locks.clear();

        for (Auction auction : store.load()) {
            auctions.put(auction.getId(), auction);
            locks.put(
                    auction.getId(),
                    new ReentrantLock()
            );
        }

        refreshStatuses();
    }

    public synchronized void addAuction(Auction auction) {
        if (auction == null) {
            throw new IllegalArgumentException(
                    "Phiên đấu giá không hợp lệ"
            );
        }

        if (auctions.containsKey(auction.getId())) {
            throw new IllegalArgumentException(
                    "Mã phiên đấu giá đã tồn tại"
            );
        }

        auctions.put(auction.getId(), auction);

        locks.put(
                auction.getId(),
                new ReentrantLock()
        );

        save();
    }

    public synchronized Auction createAuction(
            String sellerUsername,
            CreateItemRequest request,
            BigDecimal startingPrice,
            BigDecimal increment,
            Instant startAt,
            Instant endAt
    ) {

        if (sellerUsername == null || sellerUsername.isBlank()) {
            throw new IllegalArgumentException(
                    "Thiếu thông tin người bán"
            );
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Thiếu thông tin sản phẩm"
            );
        }

        if (startingPrice == null || startingPrice.signum() < 0) {
            throw new IllegalArgumentException(
                    "Giá khởi điểm không hợp lệ"
            );
        }

        if (increment == null || increment.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Bước giá phải lớn hơn 0"
            );
        }

        Instant now = Instant.now();

        /*
         * Tương thích với UI hiện tại:
         * nếu Client chưa gửi startAt thì bắt đầu ngay.
         */
        if (startAt == null) {
            startAt = now;
        }

        if (endAt == null) {
            throw new IllegalArgumentException(
                    "Thiếu thời gian kết thúc"
            );
        }

        if (!startAt.isBefore(endAt)) {
            throw new IllegalArgumentException(
                    "Thời gian bắt đầu phải trước thời gian kết thúc"
            );
        }

        if (!endAt.isAfter(now)) {
            throw new IllegalArgumentException(
                    "Thời gian kết thúc phải ở tương lai"
            );
        }

        Item item = itemFactory.createItem(
                sellerIdOf(sellerUsername),
                request
        );

        Auction auction = new Auction(
                UUID.randomUUID().toString(),
                item,
                startingPrice,
                increment,
                startAt,
                endAt
        );

        addAuction(auction);

        return auction;
    }

    public synchronized Auction createAuction(
            String sellerUsername,
            CreateItemRequest request,
            BigDecimal startingPrice,
            BigDecimal increment,
            Instant endAt
    ) {

        return createAuction(
                sellerUsername,
                request,
                startingPrice,
                increment,
                Instant.now(),
                endAt
        );
    }

    public synchronized List<Auction> listAuctions() {
        refreshStatuses();

        return List.copyOf(auctions.values());
    }

    public synchronized List<Auction> listAuctionsBySeller(
            String sellerUsername
    ) {
        if (sellerUsername == null
                || sellerUsername.isBlank()) {

            return List.of();
        }

        refreshStatuses();

        long sellerId = sellerIdOf(sellerUsername);

        return auctions.values()
                .stream()
                .filter(auction ->
                        auction.getItem() != null
                                && auction.getItem()
                                .getSellerId() == sellerId
                )
                .toList();
    }

    public synchronized Auction getAuction(String auctionId) {
        refreshStatuses();

        return auctions.get(auctionId);
    }

    public List<Bid> listBids(String auctionId) {
        Auction auction = getAuction(auctionId);

        if (auction == null) {
            return List.of();
        }

        return auction.getBids();
    }

    public void addObserver(AuctionObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    public Bid placeBid(
            String auctionId,
            String bidderId,
            BigDecimal amount
    ) throws InvalidBidException, AuctionClosedException {

        Auction auction;
        ReentrantLock lock;

        synchronized (this) {
            auction = auctions.get(auctionId);
            lock = locks.get(auctionId);
        }

        if (auction == null || lock == null) {
            throw new AuctionClosedException(
                    "Không tìm thấy phiên đấu giá"
            );
        }

        AuctionEvent event;
        Bid bid;

        lock.lock();

        try {
            synchronized (auction) {
                refreshStatus(auction);

                if (auction.getStatus()
                        != AuctionStatus.RUNNING) {

                    throw new AuctionClosedException(
                            "Phiên đấu giá chưa mở hoặc đã kết thúc"
                    );
                }

                if (bidderId == null
                        || bidderId.isBlank()) {

                    throw new InvalidBidException(
                            "Thiếu thông tin người đặt giá"
                    );
                }

                if (amount == null) {
                    throw new InvalidBidException(
                            "Giá đặt không được để trống"
                    );
                }

                BigDecimal minimumPrice =
                        auction.getHighestPrice()
                                .add(auction.getIncrement());

                if (amount.compareTo(minimumPrice) < 0) {
                    throw new InvalidBidException(
                            "Giá đặt tối thiểu là "
                                    + minimumPrice
                    );
                }

                bid = new Bid(
                        UUID.randomUUID().toString(),
                        auctionId,
                        bidderId,
                        amount,
                        Instant.now()
                );

                auction.addBid(bid);

                try {
                    save();
                } catch (RuntimeException exception) {
                    throw new IllegalStateException(
                            "Không thể lưu lượt đặt giá",
                            exception
                    );
                }

                event = new AuctionEvent(
                        auctionId,
                        auction.getHighestPrice(),
                        bidderId,
                        "Có lượt đặt giá mới"
                );
            }
        } finally {
            lock.unlock();
        }

        notifyObservers(event);

        return bid;
    }

    public void refreshStatuses() {
        List<AuctionEvent> events = new ArrayList<>();
        boolean changed = false;

        synchronized (this) {
            for (Auction auction : auctions.values()) {
                ReentrantLock lock = locks.get(auction.getId());

                if (lock == null) {
                    continue;
                }

                lock.lock();

                try {
                    synchronized (auction) {
                        AuctionStatus oldStatus =
                                auction.getStatus();

                        if (refreshStatus(auction)) {
                            changed = true;

                            events.add(
                                    createStatusEvent(
                                            auction,
                                            oldStatus
                                    )
                            );
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }

            if (changed) {
                save();
            }
        }

        for (AuctionEvent event : events) {
            notifyObservers(event);
        }
    }

    private AuctionEvent createStatusEvent(
            Auction auction,
            AuctionStatus oldStatus
    ) {
        AuctionStatus newStatus = auction.getStatus();

        String message;

        if (oldStatus == AuctionStatus.OPEN
                && newStatus == AuctionStatus.RUNNING) {

            message = "Phiên đấu giá đã bắt đầu";

        } else if (newStatus == AuctionStatus.FINISHED) {

            message = "Phiên đấu giá đã kết thúc";

        } else {

            message = "Trạng thái phiên đấu giá đã thay đổi";
        }

        return new AuctionEvent(
                auction.getId(),
                auction.getHighestPrice(),
                null,
                message
        );
    }

    private void notifyObservers(AuctionEvent event) {
        for (AuctionObserver observer : observers) {
            observer.onAuctionEvent(event);
        }
    }

    private boolean refreshStatus(Auction auction) {
        Instant now = Instant.now();

        AuctionStatus oldStatus = auction.getStatus();

        if (oldStatus == AuctionStatus.OPEN
                && !now.isBefore(auction.getStartAt())) {

            auction.setStatus(AuctionStatus.RUNNING);
        }

        if ((auction.getStatus()
                == AuctionStatus.RUNNING
                || auction.getStatus()
                == AuctionStatus.OPEN)
                && !now.isBefore(auction.getEndAt())) {

            auction.setStatus(AuctionStatus.FINISHED);
        }

        return oldStatus != auction.getStatus();
    }

    private synchronized void save() {
        if (store != null) {
            store.save(
                    new ArrayList<>(auctions.values())
            );
        }
    }
}