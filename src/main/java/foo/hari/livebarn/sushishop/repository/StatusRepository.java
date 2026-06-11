package foo.hari.livebarn.sushishop.repository;

import foo.hari.livebarn.sushishop.entity.Status;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    Status getStatusByName(String name);
}

