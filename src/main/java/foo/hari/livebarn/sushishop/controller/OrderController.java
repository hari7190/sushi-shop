package foo.hari.livebarn.sushishop.controller;

import foo.hari.livebarn.sushishop.dto.*;
import foo.hari.livebarn.sushishop.entity.Sushi;
import foo.hari.livebarn.sushishop.entity.SushiOrder;
import foo.hari.livebarn.sushishop.mapper.OrderMapper;
import foo.hari.livebarn.sushishop.repository.SushiRepository;
import foo.hari.livebarn.sushishop.service.OrderService;
import foo.hari.livebarn.sushishop.task.CookOrchestrator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final SushiRepository sushiRepository;
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final CookOrchestrator cookOrchestrator;

    public OrderController(SushiRepository sushiRepository, OrderService orderService, OrderMapper orderMapper, CookOrchestrator cookOrchestrator){
        this.sushiRepository = sushiRepository;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.cookOrchestrator = cookOrchestrator;
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

    @DeleteMapping("/orders/{order_id}")
    public ResponseEntity<GenericResponseDTO> cancelOrder(@PathVariable("order_id") int orderId) {
        cookOrchestrator.cancelOrder(orderId);
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok(new GenericResponseDTO(0, "Order cancelled"));
    }

    @PutMapping("/orders/{order_id}/pause")
    public ResponseEntity<GenericResponseDTO> pauseOrder(@PathVariable("order_id") int orderId) {
        cookOrchestrator.pauseOrder(orderId);
        orderService.pauseOrder(orderId);
        return ResponseEntity.ok(new GenericResponseDTO(0, "Order paused"));
    }

    @PutMapping("/orders/{order_id}/resume")
    public ResponseEntity<GenericResponseDTO> resumeOrder(@PathVariable("order_id") int orderId) {
        orderService.resumeOrder(orderId);
        return ResponseEntity.ok(new GenericResponseDTO(0, "Order resumed"));
    }

    @GetMapping("/orders/status")
    public ResponseEntity<OrderStatusResponseDTO> getOrderStatus() {
        return ResponseEntity.ok(orderService.getOrderStatus());
    }
}
