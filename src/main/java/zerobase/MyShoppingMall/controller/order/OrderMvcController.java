package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.member.MemberService;
import zerobase.MyShoppingMall.service.order.AddressService;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderMvcController {
    private final OrderService orderService;
    private final AddressService addressService;
    private final CartService cartService;
    private final MemberRepository memberRepository;

    //주문 생성
    @PostMapping("/create")
    public String createOrderForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @ModelAttribute OrderCreateRequest request) {
        request.setMemberId(userDetails.getMember().getId());
        Long orderId = orderService.createOrder(request).getOrderId();
        return "redirect:/orders/complete-view?orderId=" + orderId;
    }

    // 내 주문 목록
    @GetMapping("/myOrder")
    public String getMyOrders(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
        model.addAttribute("orders", orders);
        return "order/details_order";
    }

    // 주문 상세 페이지
    @GetMapping("/{orderId}/order/details_order")
    public String showOrderDetails(@PathVariable Long orderId, Model model) {
        OrderResponseDto order = orderService.getOrder(orderId);
        model.addAttribute("order", order);
        return "order/details_order";
    }

    // 주문 완료 페이지
    @GetMapping("/complete-view")
    public String completeView(@RequestParam Long orderId, Model model) {
        OrderResponseDto order = orderService.getOrder(orderId);
        model.addAttribute("order", order);
        return "order/success_order";
    }

    // 주문 취소 요청
    @PostMapping("/{orderId}/request-cancel")
    public String requestCancelOrder(@PathVariable Long orderId,
                                     RedirectAttributes redirectAttributes) {
        try {
            boolean requested = orderService.requestCancelOrder(orderId);
            if (requested) {
                redirectAttributes.addFlashAttribute("message", "주문 취소 요청이 접수되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "주문 취소 요청에 실패했습니다.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/myOrder";
    }

    // 주문 취소 처리 (폼)
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam(required = false) String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            OrderResponseDto order = orderService.getOrder(orderId);
            String impUid = order.getImpUid();
            boolean cancelled = orderService.cancelOrder(orderId, reason, impUid);
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
}
