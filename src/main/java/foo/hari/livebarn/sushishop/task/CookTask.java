package foo.hari.livebarn.sushishop.task;

import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.service.OrderService;
import org.springframework.stereotype.Component;

@Component
public class CookTask {
    private final OrderService orderService;

    public CookTask(OrderService orderService){
        this.orderService = orderService;
    }

    public void doWork(SushiOrder order, JobHandle handle) {
        int remaining = order.getRemaining_time();
        System.out.println("TASKING!!!" + order.getId());
        if (remaining <= 0) {
            throw new IllegalStateException("Order " + order.getId() + " has no remaining cook time");
        }

        while (remaining > 0) {
            if (handle.cancelRequested) {
                orderService.updateRemainingTime(order, remaining);
                orderService.cancelOrder(order.getId());
                return;
            }
            if (handle.pauseRequested) {
                orderService.pauseOrder(order, remaining);
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (handle.cancelRequested) {
                    orderService.updateRemainingTime(order, remaining);
                    orderService.cancelOrder(order.getId());
                    return;
                }
                throw new RuntimeException(e);
            }
            remaining--;
            orderService.updateRemainingTime(order, remaining);
        }
        orderService.finishOrder(order);
    }
}
