package foo.hari.livebarn.sushishop.repository;

import foo.hari.livebarn.sushishop.entity.SushiOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SushiOrderRepository extends JpaRepository<SushiOrder, Integer> {
}
