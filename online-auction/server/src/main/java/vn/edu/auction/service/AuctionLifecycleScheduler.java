package vn.edu.auction.service;

import vn.edu.auction.model.Auction;
import vn.edu.auction.model.AuctionStatus;
import vn.edu.auction.repository.AuctionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Component
public class AuctionLifecycleScheduler {
    private final AuctionRepository auctions;

    public AuctionLifecycleScheduler(AuctionRepository auctions) {
        this.auctions = auctions;
    }

    // Mỗi 1 giây cập nhật trạng thái. Scheduler chỉ là cơ chế lifecycle;
    // mọi bid vẫn được kiểm tra thời gian bằng clock phía server trong transaction.
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void updateAuctionStatuses() {
        Instant now = Instant.now();
        List<Auction> all = auctions.findAll();
        for (Auction auction : all) {
            if (auction.getStatus() == AuctionStatus.OPEN && !now.isBefore(auction.getStartTime())
                    && now.isBefore(auction.getEndTime())) {
                auction.setStatus(AuctionStatus.RUNNING);
            } else if ((auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING)
                    && !now.isBefore(auction.getEndTime())) {
                auction.setStatus(AuctionStatus.FINISHED);
            }
        }
    }
}
