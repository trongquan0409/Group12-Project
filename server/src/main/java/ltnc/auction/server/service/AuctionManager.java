package ltnc.auction.server.service;
import java.math.BigDecimal; import java.time.Instant; import java.util.*; import java.util.concurrent.CopyOnWriteArrayList; import java.util.concurrent.locks.ReentrantLock;
import ltnc.auction.common.event.AuctionEvent; import ltnc.auction.common.exception.*; import ltnc.auction.common.model.*; import ltnc.auction.server.observer.AuctionObserver; import ltnc.auction.server.persistence.AuctionStore;
/** Singleton application service. Lock is held through validation, mutation and persistence. */
public final class AuctionManager { private static final AuctionManager INSTANCE=new AuctionManager(); private final Map<String,Auction> auctions=new LinkedHashMap<>(); private final Map<String,ReentrantLock> locks=new HashMap<>(); private final List<AuctionObserver> observers=new CopyOnWriteArrayList<>(); private AuctionStore store;
 private AuctionManager(){} public static AuctionManager getInstance(){return INSTANCE;}
 public synchronized void configureStore(AuctionStore s){store=s;for(Auction a:s.load()){auctions.put(a.getId(),a);locks.put(a.getId(),new ReentrantLock());}}
 public synchronized void addAuction(Auction a){auctions.put(a.getId(),a);locks.put(a.getId(),new ReentrantLock());save();}
 public synchronized List<Auction> listAuctions(){refreshStatuses();return List.copyOf(auctions.values());}
 public synchronized Auction getAuction(String id){refreshStatuses();return auctions.get(id);}
 public void addObserver(AuctionObserver o){observers.add(o);} public void removeObserver(AuctionObserver o){observers.remove(o);}
 public Bid placeBid(String auctionId,String bidderId,BigDecimal amount) throws InvalidBidException,AuctionClosedException {Auction a;ReentrantLock lock; synchronized(this){a=auctions.get(auctionId);lock=locks.get(auctionId);}if(a==null||lock==null)throw new AuctionClosedException("Không tìm thấy phiên đấu giá");AuctionEvent event;Bid bid;lock.lock();try{synchronized(a){refreshStatus(a);if(a.getStatus()!=AuctionStatus.RUNNING)throw new AuctionClosedException();if(amount==null||amount.compareTo(a.getHighestPrice().add(a.getIncrement()))<0)throw new InvalidBidException("Giá phải lớn hơn hoặc bằng "+a.getHighestPrice().add(a.getIncrement()));if(bidderId==null||bidderId.isBlank())throw new InvalidBidException("Thiếu người đặt giá");bid=new Bid(UUID.randomUUID().toString(),auctionId,bidderId,amount,Instant.now());a.addBid(bid);save();event=new AuctionEvent(auctionId,a.getHighestPrice(),bidderId,"Có lượt đặt giá mới");}}finally{lock.unlock();}for(AuctionObserver o:observers)o.onAuctionEvent(event);return bid;}
 public synchronized void refreshStatuses(){for(Auction a:auctions.values())refreshStatus(a);save();}
 private void refreshStatus(Auction a){Instant now=Instant.now();if(a.getStatus()==AuctionStatus.OPEN&&!now.isBefore(a.getStartAt()))a.setStatus(AuctionStatus.RUNNING);if((a.getStatus()==AuctionStatus.RUNNING||a.getStatus()==AuctionStatus.OPEN)&&!now.isBefore(a.getEndAt()))a.setStatus(AuctionStatus.FINISHED);}
 private synchronized void save(){if(store!=null)store.save(auctions.values());}
}
