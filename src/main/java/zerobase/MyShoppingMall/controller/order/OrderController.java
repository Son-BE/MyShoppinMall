package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.AddressService;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final AddressService addressService;

    @PostMapping("/create")
    public String createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @ModelAttribute OrderCreateRequest request,
                              Model model) {
        request.setMemberId(userDetails.getMember().getId());
        Long orderId = orderService.createOrder(request);

        OrderResponseDto order = orderService.getOrder(orderId);

        model.addAttribute("orderId", order.getOrderId());
        model.addAttribute("receiverName", order.getReceiverName());
        model.addAttribute("receiverPhone", order.getReceiverPhone());
        model.addAttribute("receiverAddress", order.getReceiverAddress());
        model.addAttribute("receiverDetailAddress", order.getReceiverDetailAddress());
        model.addAttribute("totalPrice", order.getTotalPrice());
        model.addAttribute("paymentMethod", order.getPaymentMethod());

        return "order/success_order";
    }

    @GetMapping("/{orderId}")
    @ResponseBody
    public OrderResponseDto getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/member/{memberId}")
    @ResponseBody
    public List<OrderResponseDto> getOrdersByMember(@PathVariable Long memberId) {
        return orderService.getOrdersByMember(memberId);
    }

//    @GetMapping("/new")
//    public String orderForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
//        Member member = userDetails.getMember();
//        Address defaultAddress = addressService.findDefaultAddressByMember(member.getId());
//
//        model.addAttribute("defaultAddress", defaultAddress);
//        model.addAttribute("member", member);
//    }
}
