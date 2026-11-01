
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
import ltnc.auction.common.model.Auction;
import ltnc.auction.common.model.AuctionStatus;
import ltnc.auction.common.model.Bid;
import ltnc.auction.server.observer.AuctionObserver;
import ltnc.auction.server.persistence.AuctionStore;

public final class AuctionManager {

 private static final AuctionManager INSTANCE = new AuctionManager();

 private final Map<String, Auction> auctions = new LinkedHashMap<>();
 private final Map<String, ReentrantLock> locks = new HashMap<>();

 private final List<AuctionObserver> observers =
         new CopyOnWriteArrayList<>();

 private AuctionStore store;

 private AuctionManager() {
 }

 public static AuctionManager getInstance() {
  return INSTANCE;
 }

 // Configure persistence and load saved auctions
 public synchronized void configureStore(AuctionStore store) {
  this.store = store;

  auctions.clear();
  locks.clear();

  for (Auction auction : store.load()) {
   auctions.put(auction.getId(), auction);
   locks.put(auction.getId(), new ReentrantLock());
  }

  refreshStatuses();
 }

 // Add a new auction
 public synchronized void addAuction(Auction auction) {
  if (auction == null) {
   throw new IllegalArgumentException("Phiên đấu giá không hợp lệ");
  }

  if (auctions.containsKey(auction.getId())) {
   throw new IllegalArgumentException(
           "Mã phiên đấu giá đã tồn tại"
   );
  }

  auctions.put(auction.getId(), auction);
  locks.put(auction.getId(), new ReentrantLock());

  save();
 }

 // Get all auctions
 public synchronized List<Auction> listAuctions() {
  refreshStatuses();
  return List.copyOf(auctions.values());
 }

 // Get one auction by ID
 public synchronized Auction getAuction(String auctionId) {
  refreshStatuses();
  return auctions.get(auctionId);
 }

 // Get bid history of an auction
 public List<Bid> listBids(String auctionId) {
  Auction auction = getAuction(auctionId);

  if (auction == null) {
   return List.of();
  }

  return auction.getBids();
 }

 // Register an observer
 public void addObserver(AuctionObserver observer) {
  if (observer != null) {
   observers.add(observer);
  }
 }

 // Remove an observer
 public void removeObserver(AuctionObserver observer) {
  observers.remove(observer);
 }

 // Place a bid
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

    if (auction.getStatus() != AuctionStatus.RUNNING) {
     throw new AuctionClosedException(
             "Phiên đấu giá chưa mở hoặc đã kết thúc"
     );
    }

    if (bidderId == null || bidderId.isBlank()) {
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
            auction.getHighestPrice().add(
                    auction.getIncrement()
            );

    if (amount.compareTo(minimumPrice) < 0) {
     throw new InvalidBidException(
             "Giá đặt tối thiểu là " + minimumPrice
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
     // Avoid reporting a successful bid if persistence fails
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

  // Notify observers after the bid has been saved
  for (AuctionObserver observer : observers) {
   observer.onAuctionEvent(event);
  }

  return bid;
 }

 // Refresh statuses of all auctions
 public synchronized void refreshStatuses() {
  boolean changed = false;

  for (Auction auction : auctions.values()) {
   if (refreshStatus(auction)) {
    changed = true;
   }
  }

  if (changed) {
   save();
  }
 }

 // Refresh the status of one auction
 private boolean refreshStatus(Auction auction) {
  Instant now = Instant.now();
  AuctionStatus oldStatus = auction.getStatus();

  if (oldStatus == AuctionStatus.OPEN
          && !now.isBefore(auction.getStartAt())) {

   auction.setStatus(AuctionStatus.RUNNING);
  }

  if ((auction.getStatus() == AuctionStatus.RUNNING
          || auction.getStatus() == AuctionStatus.OPEN)
          && !now.isBefore(auction.getEndAt())) {

   auction.setStatus(AuctionStatus.FINISHED);
  }

  return oldStatus != auction.getStatus();
 }

 // Save auctions to persistent storage
 private synchronized void save() {
  if (store != null) {
   store.save(new ArrayList<>(auctions.values()));
  }
 }
}
