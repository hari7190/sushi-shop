package foo.hari.livebarn.sushishop.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.Future;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class JobHandle {
    int orderId;
    volatile boolean pauseRequested = false;
    Future<?> future;

    public JobHandle(int id) {
        this.orderId = id;
    }
}
