package vn.edu.auction.repository;
import vn.edu.auction.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ItemRepository extends JpaRepository<Item, Long> {}
