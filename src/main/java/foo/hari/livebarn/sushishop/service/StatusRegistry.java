package foo.hari.livebarn.sushishop.service;

import foo.hari.livebarn.sushishop.domain.OrderStatus;
import foo.hari.livebarn.sushishop.entity.Status;
import foo.hari.livebarn.sushishop.repository.StatusRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class StatusRegistry {

    private final StatusRepository statusRepository;
    private volatile Map<OrderStatus, Integer> idsByStatus;
    private volatile Map<Integer, OrderStatus> statusById;

    public StatusRegistry(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public int id(OrderStatus status) {
        ensureLoaded();
        return idsByStatus.get(status);
    }

    public OrderStatus fromId(int statusId) {
        ensureLoaded();
        OrderStatus status = statusById.get(statusId);
        if (status == null) {
            throw new IllegalStateException("Unknown status id: " + statusId);
        }
        return status;
    }

    private void ensureLoaded() {
        if (idsByStatus != null) {
            return;
        }
        synchronized (this) {
            if (idsByStatus != null) {
                return;
            }
            Map<String, OrderStatus> statusByDbName = Arrays.stream(OrderStatus.values())
                    .collect(Collectors.toMap(OrderStatus::dbName, Function.identity()));

            Map<OrderStatus, Integer> ids = new EnumMap<>(OrderStatus.class);
            Map<Integer, OrderStatus> byId = new java.util.HashMap<>();

            for (Status status : statusRepository.findAll()) {
                OrderStatus orderStatus = statusByDbName.get(status.getName());
                if (orderStatus == null) {
                    throw new IllegalStateException("Unknown status in database: " + status.getName());
                }
                ids.put(orderStatus, status.getId());
                byId.put(status.getId(), orderStatus);
            }

            for (OrderStatus orderStatus : OrderStatus.values()) {
                if (!ids.containsKey(orderStatus)) {
                    throw new IllegalStateException("Missing status in database: " + orderStatus.dbName());
                }
            }

            this.idsByStatus = Map.copyOf(ids);
            this.statusById = Map.copyOf(byId);
        }
    }
}
