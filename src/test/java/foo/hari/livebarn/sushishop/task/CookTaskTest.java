package foo.hari.livebarn.sushishop.task;

import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CookTaskTest {

    @Mock
    OrderService orderService;

    CookTask cookTask;

    @BeforeEach
    void setUp() {
        cookTask = new CookTask(orderService);
    }

    @Test
    void doWork_finishesOrderAfterRemainingTicks() {
        SushiOrder order = orderWithRemaining(1);
        JobHandle handle = new JobHandle(order.getId());

        cookTask.doWork(order, handle);

        verify(orderService).finishOrder(order);
        verify(orderService, never()).pauseOrder(order, 1);
    }

    @Test
    void doWork_pausesWhenRequested() throws InterruptedException {
        SushiOrder order = orderWithRemaining(5);
        JobHandle handle = new JobHandle(order.getId());

        Thread worker = new Thread(() -> cookTask.doWork(order, handle));
        worker.start();
        handle.pauseRequested = true;
        worker.join(5_000);

        verify(orderService).pauseOrder(order, order.getRemaining_time());
        verify(orderService, never()).finishOrder(order);
    }

    @Test
    void doWork_throwsWhenNoRemainingTime() {
        SushiOrder order = orderWithRemaining(0);
        JobHandle handle = new JobHandle(order.getId());

        assertThrows(IllegalStateException.class, () -> cookTask.doWork(order, handle));
        verify(orderService, never()).finishOrder(order);
        verify(orderService, never()).pauseOrder(order, 0);
    }

    private static SushiOrder orderWithRemaining(int remaining) {
        SushiOrder order = new SushiOrder();
        order.setId(1);
        order.setRemaining_time(remaining);
        return order;
    }
}
