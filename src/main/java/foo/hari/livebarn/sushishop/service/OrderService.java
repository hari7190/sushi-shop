package foo.hari.livebarn.sushishop.service;

import foo.hari.livebarn.sushishop.dto.OrderStatusEntryDTO;
import foo.hari.livebarn.sushishop.dto.OrderStatusResponseDTO;
import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.repository.StatusRepository;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final StatusRepository statusRepository;
    private final SushiOrderRepository sushiOrderRepository;
    private final SushiRepository sushiRepository;

    public OrderService(StatusRepository statusRepository, SushiOrderRepository sushiOrderRepository, SushiRepository sushiRepository) {
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
        this.sushiRepository = sushiRepository;
    }

    public SushiOrder placeOrder(Sushi sushi){
        SushiOrder order = new SushiOrder();
        order.setSushi_id(sushi.getId());
        order.setStatusId(statusRepository.getStatusByName("created").getId());
        order.setRemaining_time(sushi.getTimeToMake());
        sushiOrderRepository.save(order);
        return order;
    }

    public void finishOrder(SushiOrder order){
        order.setStatusId(statusRepository.getStatusByName("finished").getId());
        order.setRemaining_time(0);
        sushiOrderRepository.save(order);
    }

    public void pauseOrder(SushiOrder order, int remainingTime){
        order.setStatusId(statusRepository.getStatusByName("paused").getId());
        order.setRemaining_time(remainingTime);
        sushiOrderRepository.save(order);
    }

    public SushiOrder pauseOrder(int orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatusId(statusRepository.getStatusByName("paused").getId());
        sushiOrderRepository.save(order);
        return order;
    }

    public SushiOrder resumeOrder(int orderId) {
        SushiOrder order = sushiOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatusId() != statusRepository.getStatusByName("paused").getId()) {
            throw new RuntimeException("Order cannot be resumed");
        }
        order.setStatusId(statusRepository.getStatusByName("in-progress").getId());
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
        order.setStatusId(statusRepository.getStatusByName("cancelled").getId());
        sushiOrderRepository.save(order);
        return order;
    }

    public OrderStatusResponseDTO getOrderStatus() {
        Map<Integer, String> statusNamesById = statusRepository.findAll().stream()
                .collect(Collectors.toMap(status -> status.getId(), status -> status.getName()));
        Map<Integer, Integer> sushiTimesById = sushiRepository.findAll().stream()
                .collect(Collectors.toMap(Sushi::getId, Sushi::getTimeToMake));

        List<OrderStatusEntryDTO> inProgress = new ArrayList<>();
        List<OrderStatusEntryDTO> created = new ArrayList<>();
        List<OrderStatusEntryDTO> paused = new ArrayList<>();
        List<OrderStatusEntryDTO> cancelled = new ArrayList<>();
        List<OrderStatusEntryDTO> completed = new ArrayList<>();

        for (SushiOrder order : sushiOrderRepository.findAll()) {
            String statusName = statusNamesById.get(order.getStatusId());
            int timeToMake = sushiTimesById.get(order.getSushi_id());
            int timeSpent = timeToMake - order.getRemaining_time();
            OrderStatusEntryDTO entry = new OrderStatusEntryDTO(order.getId(), timeSpent);

            switch (statusName) {
                case "in-progress" -> inProgress.add(entry);
                case "created" -> created.add(entry);
                case "paused" -> paused.add(entry);
                case "cancelled" -> cancelled.add(entry);
                case "finished" -> completed.add(entry);
                default -> { }
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
        int inProgressId = statusRepository.getStatusByName("in-progress").getId();
        int createdId = statusRepository.getStatusByName("created").getId();

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
        int createdId = statusRepository.getStatusByName("created").getId();
        int inProgressId = statusRepository.getStatusByName("in-progress").getId();
        return sushiOrderRepository
                .findFirstByStatusIdOrderByCreatedAtAsc(createdId)
                .map(order -> {
                    order.setStatusId(inProgressId);
                    return sushiOrderRepository.saveAndFlush(order);
                });
    }


}
