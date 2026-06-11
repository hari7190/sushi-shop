package foo.hari.livebarn.sushishop.repository;

import foo.hari.livebarn.sushishop.entity.Sushi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SushiRepository extends JpaRepository<Sushi, Integer> {
    Sushi findSushiByName(String name);
}
