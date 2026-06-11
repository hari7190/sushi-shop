package foo.hari.livebarn.sushishop.controller;

import foo.hari.livebarn.sushishop.dto.OrderConfirmationDTO;
import foo.hari.livebarn.sushishop.dto.OrderDTO;
import foo.hari.livebarn.sushishop.dto.OrderRequestDTO;
import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.mapper.OrderMapper;
import foo.hari.livebarn.sushishop.repository.SushiOrderRepository;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import foo.hari.livebarn.sushishop.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final SushiRepository sushiRepository;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(SushiRepository sushiRepository, OrderService orderService, OrderMapper orderMapper){
        this.sushiRepository = sushiRepository;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderConfirmationDTO> placeAnOrder(@RequestBody OrderRequestDTO orderRequest){
        Sushi sushi = sushiRepository.findSushiByName(orderRequest.sushi_name());
        OrderConfirmationDTO orderConfirmationDTO;
        if(sushi != null){
            SushiOrder sushiOrder = orderService.placeOrder(sushi);
            OrderDTO orderDto = orderMapper.toDTO(sushiOrder);
            orderConfirmationDTO = new OrderConfirmationDTO(orderDto,0, "Order Created");
        } else {
            throw new RuntimeException("No Sushi Found!");
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(orderConfirmationDTO);
    }
}
