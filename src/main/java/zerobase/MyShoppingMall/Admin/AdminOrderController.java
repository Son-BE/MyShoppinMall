package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/entire")
    public String showAllOrders(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<OrderResponseDto> ordersPage = adminOrderService.getAllOrders(pageable);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());

        return "admin/orders/entire_order";
    }

    @GetMapping("/waiting_order")
    public String getWaitingOrdersForm(Model model,
                                       @RequestParam(defaultValue = "0") int page) {
        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.ORDERED, PageRequest.of(page, PAGE_SIZE));

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("pageTitle", "대기 중인 주문");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());

        return "admin/orders/waiting_order";
    }

    // 주문 상태 변경
    @PostMapping("/{orderId}/status")
    public String changeOrderStatus(@PathVariable Long orderId,
                                    @RequestParam OrderStatus status) {
        adminOrderService.updateOrderStatus(orderId, status);
        return "redirect:/admin/orders/entire_order";
    }

    @GetMapping("/cancel_requests")
    public String getCancelRequests(Model model,
                                    @RequestParam(defaultValue = "0") int page) {
        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.CANCEL_REQUESTED, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("pageTitle", "취소 요청 주문 관리");
        return "admin/orders/cancel_requests";
    }

    @PostMapping("/{orderId}/approve-cancel")
    public String approveCancelOrder(@PathVariable Long orderId,
                                     @RequestParam(required = false) String reason) {
        adminOrderService.approveCancelOrder(orderId, reason);
        return "redirect:/admin/orders/cancel_requests?success=true";
    }

    //    // 환불 요청
//    @GetMapping("/refund_order")
//    public String getRefundOrdersForm(Model model,
//                                      @RequestParam(defaultValue = "0") int page) {
//        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.RETURNED, PageRequest.of(page, PAGE_SIZE));
//        model.addAttribute("ordersPage", ordersPage);
//        model.addAttribute("pageTitle", "환불 요청 주문");
//        return "admin/orders/refund_request";
//    }

    // 주문 상세
//    @GetMapping("/{orderId}")
//    public String orderDetail(@PathVariable Long orderId, Model model) {
//        OrderResponseDto order = adminOrderService.getOrderDetail(orderId);
//        model.addAttribute("order", order);
//        return "admin/orders/order_detail";
//    }
}
