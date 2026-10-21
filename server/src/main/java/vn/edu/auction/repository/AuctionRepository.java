package vn.edu.auction.repository;

import vn.edu.auction.model.Auction;
import vn.edu.auction.model.AuctionStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findAllByOrderByEndTimeAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Auction a where a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);

    @Query("select a from Auction a where a.status in :statuses and a.endTime <= :now")
    List<Auction> findExpired(@Param("statuses") List<AuctionStatus> statuses, @Param("now") Instant now);
}
