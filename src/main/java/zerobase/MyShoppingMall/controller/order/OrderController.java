package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.Order.OrderRequestDto;
import zerobase.MyShoppingMall.dto.Order.OrderResponseDto;
import zerobase.MyShoppingMall.service.order.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderRequestDto request) {
        Long orderId = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
       return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

}
