package foo.hari.livebarn.sushishop.task;

import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CookOrchestratorTest {

    @Mock
    OrderService orderService;

    @Mock
    CookTask worker;

    CookOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new CookOrchestrator(Runnable::run, worker, orderService);
    }

    @AfterEach
    void tearDown() {
        if (orchestrator.isRunning()) {
            orchestrator.stop();
        }
    }

    @Test
    void startAndStop_manageRunningState() {
        assertFalse(orchestrator.isRunning());

        orchestrator.start();
        assertTrue(orchestrator.isRunning());

        orchestrator.stop();
        assertFalse(orchestrator.isRunning());
    }

    @Test
    void pauseOrder_setsPauseRequestedOnActiveJob() throws Exception {
        SushiOrder order = new SushiOrder();
        order.setId(42);

        JobHandle handle = new JobHandle(42);
        activeJobs(orchestrator).put(42, handle);

        orchestrator.pauseOrder(order);

        assertTrue(handle.pauseRequested);
    }

    @Test
    void orchestrate_doesNotExceedThreeConcurrentJobs() throws Exception {
        CountDownLatch firstBatchStarted = new CountDownLatch(3);
        CountDownLatch releaseWorkers = new CountDownLatch(1);

        when(orderService.claimNextCreatedOrder())
                .thenReturn(Optional.of(order(1)))
                .thenReturn(Optional.of(order(2)))
                .thenReturn(Optional.of(order(3)))
                .thenReturn(Optional.of(order(4)))
                .thenReturn(Optional.empty());

        doAnswer(invocation -> {
            firstBatchStarted.countDown();
            assertTrue(releaseWorkers.await(2, TimeUnit.SECONDS));
            return null;
        }).when(worker).doWork(any(), any());

        Executor asyncExecutor = command -> new Thread(command).start();
        orchestrator = new CookOrchestrator(asyncExecutor, worker, orderService);
        orchestrator.start();

        assertTrue(firstBatchStarted.await(2, TimeUnit.SECONDS));
        assertEquals(3, activeJobs(orchestrator).size());

        releaseWorkers.countDown();
        Thread.sleep(200);
        orchestrator.stop();
    }

    @Test
    void orchestrate_removesJobFromActiveJobsWhenWorkCompletes() throws Exception {
        when(orderService.claimNextCreatedOrder())
                .thenReturn(Optional.of(order(7)))
                .thenReturn(Optional.empty());

        doNothing().when(worker).doWork(any(), any());

        orchestrator.start();
        Thread.sleep(300);
        orchestrator.stop();

        assertFalse(activeJobs(orchestrator).containsKey(7));
        verify(worker).doWork(any(), any());
    }

    private static SushiOrder order(int id) {
        SushiOrder order = new SushiOrder();
        order.setId(id);
        order.setRemaining_time(1);
        return order;
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<Integer, JobHandle> activeJobs(CookOrchestrator orchestrator) throws Exception {
        Field field = CookOrchestrator.class.getDeclaredField("activeJobs");
        field.setAccessible(true);
        return (ConcurrentHashMap<Integer, JobHandle>) field.get(orchestrator);
    }
}
