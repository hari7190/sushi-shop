package foo.hari.livebarn.sushishop.repository;

import foo.hari.livebarn.sushishop.entity.SushiOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SushiOrderRepository extends JpaRepository<SushiOrder, Integer> {
    Optional<SushiOrder> findFirstByStatusIdOrderByCreatedAtAsc(int statusId);

}
