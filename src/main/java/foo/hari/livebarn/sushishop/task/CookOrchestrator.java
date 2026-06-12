package foo.hari.livebarn.sushishop.task;

import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.service.OrderService;
import org.hibernate.query.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.*;

@Service
@EnableAsync
public class CookOrchestrator  implements SmartLifecycle {
    private final Executor taskWorkerExecutor;
    private final OrderService orderService;
    private final CookTask worker;
    private final ConcurrentHashMap<Integer, JobHandle> activeJobs = new ConcurrentHashMap<>();
    private volatile boolean running = false;

    public CookOrchestrator (
        @Qualifier("taskWorkerExecutor") Executor taskWorkerExecutor,
        CookTask worker, OrderService orderService) {
        this.taskWorkerExecutor = taskWorkerExecutor;
        this.worker = worker;
        this.orderService = orderService;
    }

    private final ExecutorService dispatcher = Executors.newSingleThreadExecutor(
            r -> new Thread(r, "cook-orchestrator")
    );

    public void orchestrate() {
        while (running) {
            try {
                if (activeJobs.size() >= 3) {
                    Thread.sleep(100);
                    continue;
                }

                Optional<SushiOrder> orderOpt = claimNext();
                if (orderOpt.isEmpty()) {
                    Thread.sleep(1000);
                    continue;
                }

                SushiOrder claimed = orderOpt.get();
                JobHandle handle = new JobHandle(claimed.getId());
                activeJobs.put(claimed.getId(), handle);

                try {
                    handle.future = CompletableFuture.runAsync(() -> {
                        try {
                            worker.doWork(claimed, handle);
                        } finally {
                            activeJobs.remove(claimed.getId());
                        }
                    }, taskWorkerExecutor);
                } catch (RuntimeException e) {
                    activeJobs.remove(claimed.getId());
                    Thread.sleep(100);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // log
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public boolean pauseOrder(int orderId) {
        JobHandle handle = activeJobs.get(orderId);
        if (handle != null) {
            handle.pauseRequested = true;
            return true;
        }
        return false;
    }

    public void cancelOrder(int orderId) {
        JobHandle handle = activeJobs.get(orderId);
        if (handle != null) {
            handle.cancelRequested = true;
            if (handle.future != null) {
                handle.future.cancel(true);
            }
            activeJobs.remove(orderId);
        }
    }

    private final Object claimLock = new Object();

    private Optional<SushiOrder> claimNext() {
        synchronized (claimLock) {
            return this.orderService.claimNextOrder(activeJobs.keySet());
        }
    }

    @Override
    public void start() {
        running = true;
        dispatcher.submit(this::orchestrate);
    }

    @Override
    public void stop() {
        running = false;           // exits while (running)
        dispatcher.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}

