package vn.edu.auction.repository;
import vn.edu.auction.model.BidTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BidRepository extends JpaRepository<BidTransaction, Long> {
    List<BidTransaction> findByAuctionIdOrderByCreatedAtDesc(Long auctionId);
}
