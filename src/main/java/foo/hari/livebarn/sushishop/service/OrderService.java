package foo.hari.livebarn.sushishop.service;

import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.repository.StatusRepository;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final StatusRepository statusRepository;
    private SushiOrderRepository sushiOrderRepository;

    public OrderService(StatusRepository statusRepository, SushiOrderRepository sushiOrderRepository) {
        this.statusRepository = statusRepository;
        this.sushiOrderRepository = sushiOrderRepository;
    }

    public SushiOrder placeOrder(Sushi sushi){
        SushiOrder sushiOrder = new SushiOrder();
        sushiOrder.setSushi_id(sushi.getId());
        sushiOrder.setStatus_id(statusRepository.getStatusByName("created").getId());
        sushiOrderRepository.save(sushiOrder);
        return sushiOrder;
    }
}
