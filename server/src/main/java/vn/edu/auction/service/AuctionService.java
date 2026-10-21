package vn.edu.auction.service;

import vn.edu.auction.dto.*;
import vn.edu.auction.model.*;
import vn.edu.auction.realtime.AuctionWebSocketHandler;
import vn.edu.auction.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class AuctionService {
    private final AuctionRepository auctions;
    private final BidRepository bids;
    private final UserRepository users;
    private final ItemRepository items;
    private final AuctionWebSocketHandler webSocket;

    public AuctionService(AuctionRepository auctions, BidRepository bids, UserRepository users,
                          ItemRepository items, AuctionWebSocketHandler webSocket) {
        this.auctions = auctions;
        this.bids = bids;
        this.users = users;
        this.items = items;
        this.webSocket = webSocket;
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> list() {
        return auctions.findAllByOrderByEndTimeAsc().stream().map(AuctionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AuctionResponse get(Long id) {
        return AuctionResponse.from(auctions.findById(id)
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên đấu giá " + id)));
    }

    @Transactional
    public AuctionResponse create(CreateAuctionRequest request) {
        UserAccount seller = users.findById(request.sellerId())
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Seller"));
        if (seller.getRole() != UserRole.SELLER && seller.getRole() != UserRole.ADMIN)
            throw new IllegalArgumentException("Tài khoản không có quyền tạo phiên");
        if (request.startingPrice() == null || request.startingPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Giá khởi điểm phải lớn hơn 0");
        if (request.minIncrement() == null || request.minIncrement().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Bước giá phải lớn hơn 0");
        if (request.endTime() == null || request.startTime() == null ||
            !request.endTime().isAfter(request.startTime()))
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu");

        Item item = items.save(new Item(seller.getId(), request.itemName(),
          request.description(), ItemCategory.OTHER));
        Auction auction = auctions.save(new Auction(item.getId(), seller.getId(),
          request.startingPrice(), request.minIncrement(), request.startTime(), request.endTime()));
        return AuctionResponse.from(auction);
    }

    @Transactional
    public BidResponse placeBid(Long auctionId, PlaceBidRequest request) {
        // Khóa hàng trong DB: hai request cho cùng phiên phải tuần tự kiểm tra currentPrice.
        Auction auction = auctions.findByIdForUpdate(auctionId)
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên đấu giá"));
        UserAccount bidder = users.findById(request.bidderId())
          .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người đấu giá"));

        if (bidder.getRole() != UserRole.BIDDER)
            throw new IllegalArgumentException("Chỉ Bidder mới được đặt giá");
        if (bidder.getStatus() != UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản đang bị khóa");

        Instant now = Instant.now();
        if (now.isBefore(auction.getStartTime()) || !now.isBefore(auction.getEndTime()))
            throw new IllegalArgumentException("Phiên chưa bắt đầu hoặc đã hết hạn");
        if (auction.getStatus() != AuctionStatus.RUNNING && auction.getStatus() != AuctionStatus.OPEN)
            throw new IllegalArgumentException("Phiên không nhận đặt giá");
        if (request.amount() == null ||
            request.amount().compareTo(auction.getCurrentPrice().add(auction.getMinIncrement())) < 0)
            throw new IllegalArgumentException("Giá phải >= giá hiện tại + bước giá tối thiểu");

        auction.setStatus(AuctionStatus.RUNNING);
        auction.applyBid(request.amount(), bidder.getId());
        BidTransaction bid = bids.save(new BidTransaction(auctionId, bidder.getId(), request.amount()));
        auctions.save(auction);

        // Chỉ broadcast sau khi DB commit thành công, tránh client thấy giá đã rollback.
        AuctionEvent event = new AuctionEvent("BID_UPDATED", auctionId, auction.getCurrentPrice(),
          auction.getLeadingBidderId(), auction.getEndTime(), Instant.now());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                webSocket.broadcast(event);
            }
        });
        return new BidResponse(bid.getId(), bid.getAuctionId(), bid.getBidderId(),
          bid.getAmount(), bid.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<BidResponse> history(Long auctionId) {
        if (!auctions.existsById(auctionId))
            throw new IllegalArgumentException("Không tìm thấy phiên đấu giá");
        return bids.findByAuctionIdOrderByCreatedAtDesc(auctionId).stream()
          .map(b -> new BidResponse(b.getId(), b.getAuctionId(), b.getBidderId(), b.getAmount(), b.getCreatedAt()))
          .toList();
    }
}
