package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.OrderService;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;
    private final OrderService orderService;
    private static final int PAGE_SIZE = 10;


    /**
     * 전체 주문
     */
    @GetMapping("/entire")
    public String showAllOrders(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<OrderResponseDto> ordersPage = adminOrderService.getAllOrders(pageable);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("orderStatuses", OrderStatus.values());

        return "admin/orders/entire_order";
    }


    /**
     * 대기 주문 (ORDERED 상태)
     */
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

    /**
     * 주문 상태 직접 변경
     */
    @PostMapping("/{orderId}/status")
    public String changeOrderStatus(@PathVariable Long orderId,
                                    @RequestParam OrderStatus status,
                                    RedirectAttributes redirectAttributes) {
        try {
            adminOrderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("message", "주문 상태가 변경되었습니다.");
        } catch (Exception e) {
            log.error("주문 상태 변경 실패 - orderId: {}, status: {}", orderId, status, e);
            redirectAttributes.addFlashAttribute("error", "주문 상태 변경에 실패했습니다: " + e.getMessage());
        }
        return "redirect:/admin/orders/entire";
    }

    /**
     * 취소 요청 목록
     */
    @GetMapping("/cancel_requests")
    public String getCancelRequests(Model model,
                                    @RequestParam(defaultValue = "0") int page) {
        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.CANCEL_REQUESTED, PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("pageTitle", "취소 요청 주문 관리");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        return "admin/orders/cancel_requests";
    }

    /**
     * 취소 승인 처리
     */
    @PostMapping("/{orderId}/approve-cancel")
    public String approveCancelOrder(@PathVariable Long orderId,
                                     @RequestParam(required = false) String reason,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            String adminId = userDetails.getMember().getNickName();
            boolean approved = orderService.approveCancelRequest(orderId, adminId);

            if (approved) {
                redirectAttributes.addFlashAttribute("message", "취소가 승인되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "취소 승인에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("취소 승인 실패 - orderId: {}", orderId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/cancel_requests";
    }

    /**
     * 취소 거부 처리
     */
    @PostMapping("/{orderId}/reject-cancel")
    public String rejectCancelOrder(@PathVariable Long orderId,
                                    @RequestParam(required = false) String reason,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            String adminId = userDetails.getMember().getNickName();
            boolean rejected = orderService.rejectCancelRequest(orderId, adminId);

            if (rejected) {
                redirectAttributes.addFlashAttribute("message", "취소 요청이 거부되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "취소 거부에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("취소 거부 실패 - orderId: {}", orderId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/cancel_requests";
    }

//    /**
//     * 반품 승인 처리
//     */
//    @PostMapping("/{orderId}/approve-return")
//    public String approveReturnOrder(@PathVariable Long orderId,
//                                     @RequestParam(required = false) String reason,
//                                     @AuthenticationPrincipal CustomUserDetails userDetails,
//                                     RedirectAttributes redirectAttributes) {
//        try {
//            String adminId = userDetails.getMember().getNickName();
//            boolean approved = orderService.approveReturnRequest(orderId, adminId);
//
//            if (approved) {
//                redirectAttributes.addFlashAttribute("message", "반품이 승인되었습니다.");
//            } else {
//                redirectAttributes.addFlashAttribute("error", "반품 승인에 실패했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("반품 승인 실패 - orderId: {}", orderId, e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/admin/orders/return_requests";
//    }

//    /**
//     * 반품 거부 처리
//     */
//    @PostMapping("/{orderId}/reject-return")
//    public String rejectReturnOrder(@PathVariable Long orderId,
//                                    @RequestParam(required = false) String reason,
//                                    @AuthenticationPrincipal CustomUserDetails userDetails,
//                                    RedirectAttributes redirectAttributes) {
//        try {
//            String adminId = userDetails.getMember().getNickName();
//            boolean rejected = orderService.rejectReturnRequest(orderId, adminId);
//
//            if (rejected) {
//                redirectAttributes.addFlashAttribute("message", "반품 요청이 거부되었습니다.");
//            } else {
//                redirectAttributes.addFlashAttribute("error", "반품 거부에 실패했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("반품 거부 실패 - orderId: {}", orderId, e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/admin/orders/return_requests";
//    }


//    /**
//     * 특정 상태 주문 조회
//     */
//    @GetMapping("/status/{status}")
//    public String getOrdersByStatus(@PathVariable OrderStatus status,
//                                    @RequestParam(defaultValue = "0") int page,
//                                    Model model) {
//        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(status, PageRequest.of(page, PAGE_SIZE));
//
//        model.addAttribute("ordersPage", ordersPage);
//        model.addAttribute("orders", ordersPage.getContent());
//        model.addAttribute("pageTitle", getStatusTitle(status));
//        model.addAttribute("selectedStatus", status);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", ordersPage.getTotalPages());
//
//        return "admin/orders/status_orders";
//    }

//    /**
//     * 결제 완료 주문 목록 (수동 확정 대기)
//     */
//    @GetMapping("/paid_orders")
//    public String getPaidOrders(@RequestParam(defaultValue = "0") int page, Model model) {
//        Page<OrderResponseDto> ordersPage = adminOrderService.getOrderDtosByStatus(OrderStatus.PAID, PageRequest.of(page, PAGE_SIZE));
//
//        model.addAttribute("ordersPage", ordersPage);
//        model.addAttribute("orders", ordersPage.getContent());
//        model.addAttribute("pageTitle", "결제 완료 주문 (확정 대기)");
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", ordersPage.getTotalPages());
//
//        return "admin/orders/paid_orders";
//    }

    /**
     * 주문 확정 처리 (PAID → ORDERED)
     */
    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId,
                               RedirectAttributes redirectAttributes) {
        try {
            boolean confirmed = orderService.confirmOrder(orderId);
            if (confirmed) {
                redirectAttributes.addFlashAttribute("message", "주문이 확정되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "주문 확정에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("주문 확정 실패 - orderId: {}", orderId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/orders/paid_orders";
    }

    /**
     * 여러 주문 일괄 취소 승인
     */
    @PostMapping("/batch-approve-cancel")
    public String batchApproveCancelRequests(@RequestParam List<Long> orderIds,
                                             @AuthenticationPrincipal CustomUserDetails userDetails,
                                             RedirectAttributes redirectAttributes) {
        try {
            String adminId = userDetails.getMember().getNickName();
            int successCount = 0;
            int totalCount = orderIds.size();

            for (Long orderId : orderIds) {
                try {
                    if (orderService.approveCancelRequest(orderId, adminId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("일괄 취소 승인 중 실패 - orderId: {}", orderId, e);
                }
            }

            redirectAttributes.addFlashAttribute("message",
                    String.format("총 %d개 중 %d개 주문 취소가 승인되었습니다.", totalCount, successCount));

        } catch (Exception e) {
            log.error("일괄 취소 승인 실패", e);
            redirectAttributes.addFlashAttribute("error", "일괄 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/orders/cancel_requests";
    }

    /**
     * 여러 주문 일괄 반품 승인
     */
    @PostMapping("/batch-approve-return")
    public String batchApproveReturnRequests(@RequestParam List<Long> orderIds,
                                             @AuthenticationPrincipal CustomUserDetails userDetails,
                                             RedirectAttributes redirectAttributes) {
        try {
            String adminId = userDetails.getMember().getNickName();
            int successCount = 0;
            int totalCount = orderIds.size();

            for (Long orderId : orderIds) {
                try {
                    if (orderService.approveReturnRequest(orderId, adminId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("일괄 반품 승인 중 실패 - orderId: {}", orderId, e);
                }
            }

            redirectAttributes.addFlashAttribute("message",
                    String.format("총 %d개 중 %d개 주문 반품이 승인되었습니다.", totalCount, successCount));

        } catch (Exception e) {
            log.error("일괄 반품 승인 실패", e);
            redirectAttributes.addFlashAttribute("error", "일괄 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/orders/return_requests";
    }

    /**
     * 여러 주문 일괄 확정
     */
    @PostMapping("/batch-confirm")
    public String batchConfirmOrders(@RequestParam List<Long> orderIds,
                                     RedirectAttributes redirectAttributes) {
        try {
            int successCount = 0;
            int totalCount = orderIds.size();

            for (Long orderId : orderIds) {
                try {
                    if (orderService.confirmOrder(orderId)) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.warn("일괄 주문 확정 중 실패 - orderId: {}", orderId, e);
                }
            }

            redirectAttributes.addFlashAttribute("message",
                    String.format("총 %d개 중 %d개 주문이 확정되었습니다.", totalCount, successCount));

        } catch (Exception e) {
            log.error("일괄 주문 확정 실패", e);
            redirectAttributes.addFlashAttribute("error", "일괄 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/admin/orders/paid_orders";
    }



    /**
     * 주문 상태 변경 API
     */
    @PostMapping("/api/{orderId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changeOrderStatusApi(@PathVariable Long orderId,
                                                                    @RequestParam OrderStatus status) {
        Map<String, Object> response = new HashMap<>();
        try {
            adminOrderService.updateOrderStatus(orderId, status);
            response.put("success", true);
            response.put("message", "주문 상태가 변경되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주문 상태 변경 실패 - orderId: {}, status: {}", orderId, status, e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문 통계 API
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 각 상태별 주문 수 조회
            for (OrderStatus status : OrderStatus.values()) {
                Page<OrderResponseDto> page = adminOrderService.getOrderDtosByStatus(status, PageRequest.of(0, 1));
                stats.put(status.name().toLowerCase() + "Count", page.getTotalElements());
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("주문 통계 조회 실패", e);
            return ResponseEntity.badRequest().body(Map.of("error", "통계 조회에 실패했습니다."));
        }
    }


    /**
     * 상태에 따른 페이지 제목
     */
    private String getStatusTitle(OrderStatus status) {
        return switch (status) {
            case WAITING -> "결제 대기 주문";
            case PAID -> "결제 완료 주문";
            case ORDERED -> "주문 확정 주문";
            case SHIPPED_WAITING -> "배송 대기 주문";
            case SHIPPED -> "배송 중 주문";
            case DELIVERED -> "배송 완료 주문";
            case CANCEL_REQUESTED -> "취소 요청 주문";
            case CANCEL_APPROVED -> "취소 승인 주문";
            case CANCELLED -> "취소 완료 주문";
            case RETURN_REQUESTED -> "반품 요청 주문";
            case RETURNED -> "반품 완료 주문";
        };
    }

}
