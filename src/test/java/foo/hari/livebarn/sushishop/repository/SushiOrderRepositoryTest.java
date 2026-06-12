package foo.hari.livebarn.sushishop.repository;

import foo.hari.livebarn.sushishop.entity.SushiOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class SushiOrderRepositoryTest {

    @Autowired
    SushiOrderRepository sushiOrderRepository;

    @Autowired
    StatusRepository statusRepository;

    int createdStatusId;

    @BeforeEach
    void setUp() {
        createdStatusId = statusRepository.getStatusByName("created").getId();
    }

    @Test
    void findFirstByStatusIdOrderByCreatedAtAsc_returnsOldestOrder() throws InterruptedException {
        SushiOrder oldest = saveCreatedOrder(1);
        Thread.sleep(20);
        saveCreatedOrder(2);
        Thread.sleep(20);
        saveCreatedOrder(3);

        SushiOrder found = sushiOrderRepository
                .findFirstByStatusIdOrderByCreatedAtAsc(createdStatusId)
                .orElseThrow();

        assertEquals(oldest.getId(), found.getId());
    }

    private SushiOrder saveCreatedOrder(int sushiId) {
        SushiOrder order = new SushiOrder();
        order.setStatusId(createdStatusId);
        order.setSushi_id(sushiId);
        order.setRemaining_time(10);
        return sushiOrderRepository.saveAndFlush(order);
    }
}
