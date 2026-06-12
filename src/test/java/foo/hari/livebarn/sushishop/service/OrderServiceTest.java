package foo.hari.livebarn.sushishop.service;

import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.repository.StatusRepository;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(OrderService.class)
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    SushiRepository sushiRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    SushiOrderRepository sushiOrderRepository;

    Sushi californiaRoll;
    int createdStatusId;
    int inProgressStatusId;
    int pausedStatusId;
    int finishedStatusId;

    @BeforeEach
    void setUp() {
        californiaRoll = sushiRepository.findSushiByName("California Roll");
        createdStatusId = statusRepository.getStatusByName("created").getId();
        inProgressStatusId = statusRepository.getStatusByName("in-progress").getId();
        pausedStatusId = statusRepository.getStatusByName("paused").getId();
        finishedStatusId = statusRepository.getStatusByName("finished").getId();
    }

    @Test
    void placeOrder_setsCreatedStatusAndRemainingTime() {
        SushiOrder order = orderService.placeOrder(californiaRoll);

        assertEquals(createdStatusId, order.getStatusId());
        assertEquals(californiaRoll.getId(), order.getSushi_id());
        assertEquals(californiaRoll.getTimeToMake(), order.getRemaining_time());
    }

    @Test
    void claimNextCreatedOrder_returnsEmptyWhenQueueEmpty() {
        assertTrue(orderService.claimNextCreatedOrder().isEmpty());
    }

    @Test
    void claimNextCreatedOrder_claimsOldestCreatedOrderFirst() throws InterruptedException {
        SushiOrder first = orderService.placeOrder(californiaRoll);
        Thread.sleep(20);
        SushiOrder second = orderService.placeOrder(californiaRoll);

        SushiOrder claimed = orderService.claimNextCreatedOrder().orElseThrow();

        assertEquals(first.getId(), claimed.getId());
        assertEquals(inProgressStatusId, claimed.getStatusId());
        assertEquals(createdStatusId, sushiOrderRepository.findById(second.getId()).orElseThrow().getStatusId());
    }

    @Test
    void finishOrder_setsFinishedStatusAndZeroRemainingTime() {
        SushiOrder order = orderService.placeOrder(californiaRoll);
        order.setRemaining_time(5);

        orderService.finishOrder(order);

        SushiOrder saved = sushiOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(finishedStatusId, saved.getStatusId());
        assertEquals(0, saved.getRemaining_time());
    }

    @Test
    void pauseOrder_setsPausedStatusAndPreservesRemainingTime() {
        SushiOrder order = orderService.placeOrder(californiaRoll);

        orderService.pauseOrder(order, 12);

        SushiOrder saved = sushiOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(pausedStatusId, saved.getStatusId());
        assertEquals(12, saved.getRemaining_time());
    }

    @Test
    void claimNextCreatedOrder_secondClaimReturnsEmptyWhenOnlyOneOrder() {
        orderService.placeOrder(californiaRoll);

        assertTrue(orderService.claimNextCreatedOrder().isPresent());
        assertTrue(orderService.claimNextCreatedOrder().isEmpty());
    }

    @Test
    void claimNextCreatedOrder_onlyOneThreadClaimsSameOrder() throws Exception {
        orderService.placeOrder(californiaRoll);
        TestTransaction.flagForCommit();
        TestTransaction.end();

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<SushiOrder> claimed = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> {
                ready.countDown();
                awaitStart(start);
                orderService.claimNextCreatedOrder().ifPresent(claimed::add);
            });
            Future<?> second = executor.submit(() -> {
                ready.countDown();
                awaitStart(start);
                orderService.claimNextCreatedOrder().ifPresent(claimed::add);
            });

            ready.await();
            start.countDown();
            first.get();
            second.get();
        } finally {
            executor.shutdownNow();
        }

        long inProgressCount = sushiOrderRepository.findAll().stream()
                .filter(order -> order.getStatusId() == inProgressStatusId)
                .count();
        assertEquals(1, inProgressCount);
        assertFalse(claimed.isEmpty());
    }

    private static void awaitStart(CountDownLatch start) {
        try {
            start.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
