package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.domain.Address;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.dto.payment.PaymentCompleteRequest;
import zerobase.MyShoppingMall.service.IamportService;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.AddressService;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final IamportService iamportService;


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @RequestBody OrderCreateRequest request) {
        request.setMemberId(userDetails.getMember().getId());
        Long orderId = orderService.createOrder(request);

        OrderResponseDto order = orderService.getOrder(orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("merchantUid",order.getMerchantUid());

        return ResponseEntity.ok(response);

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

    @GetMapping("/myOrder")
    public String getMyOrders(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
        model.addAttribute("orders", orders);
        return "order/details_order";
    }

    @GetMapping("/complete-view")
    public String completeView(@RequestParam Long orderId, Model model) {
        OrderResponseDto order = orderService.getOrder(orderId);
        model.addAttribute("order", order);

        model.addAttribute("orderId", order.getOrderId());
        model.addAttribute("receiverName", order.getReceiverName());
        model.addAttribute("receiverPhone", order.getReceiverPhone());

        model.addAttribute("receiverAddress", order.getReceiverAddress());
        model.addAttribute("receiverDetailAddress", order.getReceiverDetailAddress());

        model.addAttribute("totalPrice", order.getTotalPrice());
        model.addAttribute("paymentMethod", order.getPaymentMethod());

        return "order/success_order";
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeOrder(@RequestBody PaymentCompleteRequest request) {
        boolean result = orderService.markOrderAsCompleted(request.getOrderId(), request.getImpUid());

        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("주문 완료 처리 실패");
        }
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            boolean cancelled = orderService.cancelOrder(orderId, reason);
            if (cancelled) {
                redirectAttributes.addFlashAttribute("message", "주문이 성공적으로 취소되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "주문 취소에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders/" + orderId + "/order/details_order";
    }

    @GetMapping("/{orderId}/cancel")
    public String showCancelPage(@PathVariable Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "order/cancel_confirm";
    }

}
