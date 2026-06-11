package foo.hari.livebarn.sushishop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SushiShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SushiShopApplication.class, args);
    }

}
