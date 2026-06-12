package foo.hari.livebarn.sushishop.service;

import foo.hari.livebarn.sushishop.domain.OrderStatus;
import foo.hari.livebarn.sushishop.dto.OrderStatusEntryDTO;
import foo.hari.livebarn.sushishop.dto.OrderStatusResponseDTO;
import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class OrderService {

    private final StatusRegistry statusRegistry;
    private final SushiOrderRepository sushiOrderRepository;
    private final SushiRepository sushiRepository;

    public OrderService(StatusRegistry statusRegistry, SushiOrderRepository sushiOrderRepository, SushiRepository sushiRepository) {
        this.statusRegistry = statusRegistry;
        this.sushiOrderRepository = sushiOrderRepository;
        this.sushiRepository = sushiRepository;
    }

    public SushiOrder placeOrder(Sushi sushi){
        SushiOrder order = new SushiOrder();
        order.setSushi_id(sushi.getId());
        order.setStatusId(statusRegistry.id(OrderStatus.CREATED));
        order.setRemaining_time(sushi.getTimeToMake());
        sushiOrderRepository.save(order);
        return order;
    }

    public void finishOrder(SushiOrder order){
        order.setStatusId(statusRegistry.id(OrderStatus.FINISHED));
        order.setRemaining_time(0);
        sushiOrderRepository.save(order);
    }

    public void pauseOrder(SushiOrder order, int remainingTime){
        order.setStatusId(statusRegistry.id(OrderStatus.PAUSED));
        order.setRemaining_time(remainingTime);
        sushiOrderRepository.save(order);
    }

    public SushiOrder pauseOrder(int orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatusId(statusRegistry.id(OrderStatus.PAUSED));
        sushiOrderRepository.save(order);
        return order;
    }

    public SushiOrder resumeOrder(int orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatusId() != statusRegistry.id(OrderStatus.PAUSED)) {
            throw new RuntimeException("Order cannot be resumed");
        }
        order.setStatusId(statusRegistry.id(OrderStatus.IN_PROGRESS));
        sushiOrderRepository.save(order);
        return order;
    }

    public void updateRemainingTime(SushiOrder order, int remainingTime) {
        order.setRemaining_time(remainingTime);
        sushiOrderRepository.save(order);
    }

    public SushiOrder cancelOrder(int orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatusId(statusRegistry.id(OrderStatus.CANCELLED));
        sushiOrderRepository.save(order);
        return order;
    }

    public OrderStatusResponseDTO getOrderStatus() {
        var sushiTimesById = sushiRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Sushi::getId, Sushi::getTimeToMake));

        List<OrderStatusEntryDTO> inProgress = new ArrayList<>();
        List<OrderStatusEntryDTO> created = new ArrayList<>();
        List<OrderStatusEntryDTO> paused = new ArrayList<>();
        List<OrderStatusEntryDTO> cancelled = new ArrayList<>();
        List<OrderStatusEntryDTO> completed = new ArrayList<>();

        for (SushiOrder order : sushiOrderRepository.findAll()) {
            OrderStatus status = statusRegistry.fromId(order.getStatusId());
            int timeToMake = sushiTimesById.get(order.getSushi_id());
            int timeSpent = timeToMake - order.getRemaining_time();
            OrderStatusEntryDTO entry = new OrderStatusEntryDTO(order.getId(), timeSpent);

            switch (status) {
                case IN_PROGRESS -> inProgress.add(entry);
                case CREATED -> created.add(entry);
                case PAUSED -> paused.add(entry);
                case CANCELLED -> cancelled.add(entry);
                case FINISHED -> completed.add(entry);
            }
        }

        Comparator<OrderStatusEntryDTO> byOrderId = Comparator.comparingInt(OrderStatusEntryDTO::orderId);
        inProgress.sort(byOrderId);
        created.sort(byOrderId);
        paused.sort(byOrderId);
        cancelled.sort(byOrderId);
        completed.sort(byOrderId);

        return new OrderStatusResponseDTO(inProgress, created, paused, cancelled, completed);
    }

    @Transactional
    public Optional<SushiOrder> claimNextOrder(Set<Integer> activeOrderIds) {
        int inProgressId = statusRegistry.id(OrderStatus.IN_PROGRESS);
        int createdId = statusRegistry.id(OrderStatus.CREATED);

        Optional<SushiOrder> waitingInProgress = sushiOrderRepository
                .findByStatusIdOrderByCreatedAtAsc(inProgressId)
                .stream()
                .filter(order -> !activeOrderIds.contains(order.getId()))
                .findFirst();
        if (waitingInProgress.isPresent()) {
            return waitingInProgress;
        }

        return sushiOrderRepository
                .findFirstByStatusIdOrderByCreatedAtAsc(createdId)
                .map(order -> {
                    order.setStatusId(inProgressId);
                    return sushiOrderRepository.saveAndFlush(order);
                });
    }

    @Transactional
    public Optional<SushiOrder> claimNextCreatedOrder() {
        int createdId = statusRegistry.id(OrderStatus.CREATED);
        int inProgressId = statusRegistry.id(OrderStatus.IN_PROGRESS);
        return sushiOrderRepository
                .findFirstByStatusIdOrderByCreatedAtAsc(createdId)
                .map(order -> {
                    order.setStatusId(inProgressId);
                    return sushiOrderRepository.saveAndFlush(order);
                });
    }


}
