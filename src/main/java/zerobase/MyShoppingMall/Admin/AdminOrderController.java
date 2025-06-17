package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.type.OrderStatus;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private static final int PAGE_SIZE = 10;

    // 전체 주문
    @GetMapping("/entire_order")
    public String getAllOrdersForm(Model model,
                                   @RequestParam(defaultValue = "0") int page) {
        Page<OrderResponseDto> ordersPage = adminOrderService.getAllOrders(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("pageTitle", "전체 주문");
        return "admin/orders/entire_order";
    }

    // 대기 주문
    @GetMapping("/waiting_order")
    public String getWaitingOrdersForm(Model model,
                                       @RequestParam(defaultValue = "0") int page) {
        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.WAITING, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("pageTitle", "대기 중인 주문");
        return "admin/orders/waiting_order";
    }

    // 환불 요청
    @GetMapping("/refund_order")
    public String getRefundOrdersForm(Model model,
                                      @RequestParam(defaultValue = "0") int page) {
        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.RETURNED, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("pageTitle", "환불 요청 주문");
        return "admin/orders/refund_request";
    }

    // 주문 상세
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model) {
        OrderResponseDto order = adminOrderService.getOrderDetail(orderId);
        model.addAttribute("order", order);
        return "admin/orders/order_detail";
    }

    // 주문 상태 변경
    @PostMapping("/{orderId}/status")
    public String changeOrderStatus(@PathVariable Long orderId,
                                    @RequestParam OrderStatus status) {
        adminOrderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders/entire_order";
    }
}
