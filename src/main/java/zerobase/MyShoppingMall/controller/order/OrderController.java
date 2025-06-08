package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.util.List;


@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/create")
    @ResponseBody
    public String createOrder(@ModelAttribute OrderCreateRequest request) {
        orderService.createOrder(request);
        return "redirect:/order/input_order";
    }

    @GetMapping("/{orderId}")
    public OrderResponseDto getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/member/{memberId}")
    public List<OrderResponseDto> getOrdersByMember(@PathVariable Long memberId) {
        return orderService.getOrdersByMember(memberId);
    }
}
