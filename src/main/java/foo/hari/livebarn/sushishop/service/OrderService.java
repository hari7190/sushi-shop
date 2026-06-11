package foo.hari.livebarn.sushishop.service;

import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.repository.StatusRepository;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderService {

    private final StatusRepository statusRepository;
    private final SushiOrderRepository sushiOrderRepository;

    public OrderService(StatusRepository statusRepository, SushiOrderRepository sushiOrderRepository) {
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
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
