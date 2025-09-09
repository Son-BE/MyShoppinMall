package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.OrderService;
import zerobase.MyShoppingMall.temps.RecommendationService;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderMvcController {
    private final OrderService orderService;
    private final RecommendationService recommendationService;

    /**
     * 주문 생성
     */
    @PostMapping("/create")
    public String createOrderForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @ModelAttribute OrderCreateRequest request,
                                  RedirectAttributes redirectAttributes) {
        try {
            request.setMemberId(userDetails.getMember().getId());
            var createResponse = orderService.createOrder(request);
            return "redirect:/orders/complete-view?orderId=" + createResponse.getOrderId();
        } catch (Exception e) {
            log.error("주문 생성 실패", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * 내 주문 목록
     */
    @GetMapping("/myOrder")
    public String getMyOrders(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long memberId = userDetails.getMember().getId();
            List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
            model.addAttribute("orders", orders);
            model.addAttribute("orderStatuses", OrderStatus.values());

            // 추천 시스템: 주문 목록 페이지 추천 추가
            addOrderListPageRecommendations(model, memberId, orders);

            return "order/details_order";
        } catch (Exception e) {
            log.error("주문 목록 조회 실패", e);
            model.addAttribute("error", "주문 목록을 불러올 수 없습니다.");
            return "order/details_order";
        }
    }

    /**
     * 상태별 내 주문 목록
     */
    @GetMapping("/myOrder/status/{status}")
    public String getMyOrdersByStatus(@PathVariable OrderStatus status,
                                      Model model,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long memberId = userDetails.getMember().getId();
            List<OrderResponseDto> orders = orderService.getOrdersByMemberAndStatus(memberId, status);
            model.addAttribute("orders", orders);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("orderStatuses", OrderStatus.values());

            // 추천 시스템: 상태별 주문 목록 추천 추가
            addOrderListPageRecommendations(model, memberId, orders);

            return "order/details_order";
        } catch (Exception e) {
            log.error("상태별 주문 목록 조회 실패 - status: {}", status, e);
            model.addAttribute("error", "주문 목록을 불러올 수 없습니다.");
            return "order/details_order";
        }
    }

    /**
     * 주문 상세 페이지
     */
    @GetMapping("/{orderId}/details")
    public String showOrderDetails(@PathVariable Long orderId,
                                   Model model,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            OrderResponseDto order = orderService.getOrder(orderId);

            // 본인 주문만 조회 가능하도록 검증
            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
                model.addAttribute("error", "접근 권한이 없습니다.");
                return "order/details_order";
            }

            model.addAttribute("order", order);
            model.addAttribute("actualPrice", order.getActualPrice());
            model.addAttribute("usedPoint", order.getUsedPoint());

            // 추천 시스템: 주문 상세 페이지 추천 추가
            addOrderDetailPageRecommendations(model, userDetails.getMember().getId(), order);

            return "order/details_order";
        } catch (Exception e) {
            log.error("주문 상세 조회 실패 - orderId: {}", orderId, e);
            model.addAttribute("error", "주문 정보를 불러올 수 없습니다.");
            return "order/details_order";
        }
    }

    /**
     * 주문 완료 페이지
     */
    @GetMapping("/complete-view")
    public String completeView(@RequestParam Long orderId,
                               Model model,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            OrderResponseDto order = orderService.getOrder(orderId);

            // 본인 주문만 조회 가능하도록 검증
            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
                model.addAttribute("error", "접근 권한이 없습니다.");
                return "order/success_order";
            }

            model.addAttribute("order", order);

            // 추천 시스템: 주문 완료 페이지 추천 추가
            addOrderCompletePageRecommendations(model, userDetails.getMember().getId(), order);

            return "order/success_order";
        } catch (Exception e) {
            log.error("주문 완료 페이지 조회 실패 - orderId: {}", orderId, e);
            model.addAttribute("error", "주문 정보를 불러올 수 없습니다.");
            return "order/success_order";
        }
    }

    /**
     * 주문 확정 (PAID → ORDERED) - 사용자가 직접 확정하는 경우
     */
    @PostMapping("/{orderId}/confirm")
    public String confirmOrder(@PathVariable Long orderId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            // 본인 주문인지 확인
            OrderResponseDto order = orderService.getOrder(orderId);
            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                return "redirect:/orders/myOrder";
            }

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
        return "redirect:/orders/" + orderId + "/details";
    }

    /**
     * 즉시 주문 취소 (WAITING/PAID → CANCELLED)
     */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId,
                              @RequestParam(required = false) String reason,
                              @AuthenticationPrincipal CustomUserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        try {
            // 본인 주문인지 확인
            OrderResponseDto order = orderService.getOrder(orderId);
            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                return "redirect:/orders/myOrder";
            }

            boolean cancelled = orderService.cancelOrder(orderId, reason != null ? reason : "사용자 요청");
            if (cancelled) {
                redirectAttributes.addFlashAttribute("message", "주문이 성공적으로 취소되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "주문 취소에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("주문 취소 실패 - orderId: {}", orderId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/" + orderId + "/details";
    }

    /**
     * 주문 취소 요청 (ORDERED → CANCEL_REQUESTED)
     */
    @PostMapping("/{orderId}/request-cancel")
    public String requestCancelOrder(@PathVariable Long orderId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        try {
            // 본인 주문인지 확인
            OrderResponseDto order = orderService.getOrder(orderId);
            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                return "redirect:/orders/myOrder";
            }

            boolean requested = orderService.requestCancelOrder(orderId);
            if (requested) {
                redirectAttributes.addFlashAttribute("message", "주문 취소 요청이 접수되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "주문 취소 요청에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("주문 취소 요청 실패 - orderId: {}", orderId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/myOrder";
    }

    /**
     * 반품 요청 (ORDERED → RETURN_REQUESTED)
     */
    @PostMapping("/{orderId}/request-return")
    public String requestReturn(@PathVariable Long orderId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            // 본인 주문인지 확인
            OrderResponseDto order = orderService.getOrder(orderId);
            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
                return "redirect:/orders/myOrder";
            }

            boolean requested = orderService.requestReturn(orderId);
            if (requested) {
                redirectAttributes.addFlashAttribute("message", "반품 요청이 접수되었습니다.");
            } else {
                redirectAttributes.addFlashAttribute("error", "반품 요청에 실패했습니다.");
            }
        } catch (Exception e) {
            log.error("반품 요청 실패 - orderId: {}", orderId, e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/orders/myOrder";
    }

    // === 추천 시스템 관련 메서드 ===

    /**
     * 주문 목록 페이지에 추천 상품 추가
     */
    private void addOrderListPageRecommendations(Model model, Long memberId, List<OrderResponseDto> orders) {
        try {
            // 1. 재구매 추천 - 과거 구매한 상품과 유사한 상품
            if (!orders.isEmpty()) {
                // TODO: 주문 상세에서 상품 ID 추출 필요 (실제 구현시)
                // 여기서는 예시로 개인화 추천 사용
                var repurchaseRecs = recommendationService.getRecommendations(memberId, 6, "user_based");
                model.addAttribute("repurchaseRecommendations", repurchaseRecs.getRecommendations());
            }

            // 2. 새로운 상품 추천
            Map<String, Object> newItems = recommendationService.getPopularItems(8);
            model.addAttribute("newItemRecommendations", newItems.get("recommendations"));

            log.debug("주문 목록 페이지 추천 추가 완료 - 사용자: {}", memberId);

        } catch (Exception e) {
            log.warn("주문 목록 페이지 추천 추가 실패 - 사용자: {}", memberId, e);
        }
    }

    /**
     * 주문 상세 페이지에 추천 상품 추가
     */
    private void addOrderDetailPageRecommendations(Model model, Long memberId, OrderResponseDto order) {
        try {
            // 1. 관련 상품 추천 (주문한 상품과 유사한 상품)
            // TODO: 주문 상세에서 상품 ID 추출 필요 (실제 구현시)

            // 2. 개인화 추천
            var personalRecs = recommendationService.getRecommendations(memberId, 6, "content_based");
            model.addAttribute("relatedRecommendations", personalRecs.getRecommendations());

            log.debug("주문 상세 페이지 추천 추가 완료 - 주문: {}", order.getOrderId());

        } catch (Exception e) {
            log.warn("주문 상세 페이지 추천 추가 실패 - 주문: {}", order.getOrderId(), e);
        }
    }

    /**
     * 주문 완료 페이지에 추천 상품 추가
     */
    private void addOrderCompletePageRecommendations(Model model, Long memberId, OrderResponseDto order) {
        try {
            // 1. 다음 구매 추천 - 개인화 추천
            var nextPurchaseRecs = recommendationService.getRecommendations(memberId, 6, "hybrid");
            model.addAttribute("nextPurchaseRecommendations", nextPurchaseRecs.getRecommendations());

            // 2. 인기 상품 추천
            Map<String, Object> trendingItems = recommendationService.getPopularItems(4);
            model.addAttribute("trendingRecommendations", trendingItems.get("recommendations"));

            log.debug("주문 완료 페이지 추천 추가 완료 - 주문: {}", order.getOrderId());

        } catch (Exception e) {
            log.warn("주문 완료 페이지 추천 추가 실패 - 주문: {}", order.getOrderId(), e);
        }
    }
//    /**
//     * 주문 생성
//     */
//    @PostMapping("/create")
//    public String createOrderForm(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                  @ModelAttribute OrderCreateRequest request,
//                                  RedirectAttributes redirectAttributes) {
//        try {
//            request.setMemberId(userDetails.getMember().getId());
//            var createResponse = orderService.createOrder(request);
//            return "redirect:/orders/complete-view?orderId=" + createResponse.getOrderId();
//        } catch (Exception e) {
//            log.error("주문 생성 실패", e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//            return "redirect:/cart";
//        }
//    }
//
//    /**
//     * 내 주문 목록
//     */
//    @GetMapping("/myOrder")
//    public String getMyOrders(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
//        try {
//            Long memberId = userDetails.getMember().getId();
//            List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
//            model.addAttribute("orders", orders);
//            model.addAttribute("orderStatuses", OrderStatus.values());
//            return "order/details_order";
//        } catch (Exception e) {
//            log.error("주문 목록 조회 실패", e);
//            model.addAttribute("error", "주문 목록을 불러올 수 없습니다.");
//            return "order/details_order";
//        }
//    }
//
//    /**
//     * 상태별 내 주문 목록
//     */
//    @GetMapping("/myOrder/status/{status}")
//    public String getMyOrdersByStatus(@PathVariable OrderStatus status,
//                                      Model model,
//                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
//        try {
//            Long memberId = userDetails.getMember().getId();
//            List<OrderResponseDto> orders = orderService.getOrdersByMemberAndStatus(memberId, status);
//            model.addAttribute("orders", orders);
//            model.addAttribute("selectedStatus", status);
//            model.addAttribute("orderStatuses", OrderStatus.values());
//            return "order/details_order";
//        } catch (Exception e) {
//            log.error("상태별 주문 목록 조회 실패 - status: {}", status, e);
//            model.addAttribute("error", "주문 목록을 불러올 수 없습니다.");
//            return "order/details_order";
//        }
//    }
//
//    /**
//     * 주문 상세 페이지
//     */
//    @GetMapping("/{orderId}/details")
//    public String showOrderDetails(@PathVariable Long orderId,
//                                   Model model,
//                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
//        try {
//            OrderResponseDto order = orderService.getOrder(orderId);
//
//            // 본인 주문만 조회 가능하도록 검증
//            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "order/details_order";
//            }
//
//            model.addAttribute("order", order);
//            model.addAttribute("actualPrice", order.getActualPrice());
//            model.addAttribute("usedPoint", order.getUsedPoint());
//            return "order/details_order";
//        } catch (Exception e) {
//            log.error("주문 상세 조회 실패 - orderId: {}", orderId, e);
//            model.addAttribute("error", "주문 정보를 불러올 수 없습니다.");
//            return "order/details_order";
//        }
//    }
//
//    /**
//     * 주문 완료 페이지
//     */
//    @GetMapping("/complete-view")
//    public String completeView(@RequestParam Long orderId,
//                               Model model,
//                               @AuthenticationPrincipal CustomUserDetails userDetails) {
//        try {
//            OrderResponseDto order = orderService.getOrder(orderId);
//
//            // 본인 주문만 조회 가능하도록 검증
//            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
//                model.addAttribute("error", "접근 권한이 없습니다.");
//                return "order/success_order";
//            }
//
//            model.addAttribute("order", order);
//            return "order/success_order";
//        } catch (Exception e) {
//            log.error("주문 완료 페이지 조회 실패 - orderId: {}", orderId, e);
//            model.addAttribute("error", "주문 정보를 불러올 수 없습니다.");
//            return "order/success_order";
//        }
//    }
//
//    /**
//     * 주문 확정 (PAID → ORDERED) - 사용자가 직접 확정하는 경우
//     */
//    @PostMapping("/{orderId}/confirm")
//    public String confirmOrder(@PathVariable Long orderId,
//                               @AuthenticationPrincipal CustomUserDetails userDetails,
//                               RedirectAttributes redirectAttributes) {
//        try {
//            // 본인 주문인지 확인
//            OrderResponseDto order = orderService.getOrder(orderId);
//            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
//                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
//                return "redirect:/orders/myOrder";
//            }
//
//            boolean confirmed = orderService.confirmOrder(orderId);
//            if (confirmed) {
//                redirectAttributes.addFlashAttribute("message", "주문이 확정되었습니다.");
//            } else {
//                redirectAttributes.addFlashAttribute("error", "주문 확정에 실패했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("주문 확정 실패 - orderId: {}", orderId, e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/orders/" + orderId + "/details";
//    }
//
//    /**
//     * 즉시 주문 취소 (WAITING/PAID → CANCELLED)
//     */
//    @PostMapping("/{orderId}/cancel")
//    public String cancelOrder(@PathVariable Long orderId,
//                              @RequestParam(required = false) String reason,
//                              @AuthenticationPrincipal CustomUserDetails userDetails,
//                              RedirectAttributes redirectAttributes) {
//        try {
//            // 본인 주문인지 확인
//            OrderResponseDto order = orderService.getOrder(orderId);
//            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
//                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
//                return "redirect:/orders/myOrder";
//            }
//
//            boolean cancelled = orderService.cancelOrder(orderId, reason != null ? reason : "사용자 요청");
//            if (cancelled) {
//                redirectAttributes.addFlashAttribute("message", "주문이 성공적으로 취소되었습니다.");
//            } else {
//                redirectAttributes.addFlashAttribute("error", "주문 취소에 실패했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("주문 취소 실패 - orderId: {}", orderId, e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/orders/" + orderId + "/details";
//    }
//
//    /**
//     * 주문 취소 요청 (ORDERED → CANCEL_REQUESTED)
//     */
//    @PostMapping("/{orderId}/request-cancel")
//    public String requestCancelOrder(@PathVariable Long orderId,
//                                     @AuthenticationPrincipal CustomUserDetails userDetails,
//                                     RedirectAttributes redirectAttributes) {
//        try {
//            // 본인 주문인지 확인
//            OrderResponseDto order = orderService.getOrder(orderId);
//            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
//                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
//                return "redirect:/orders/myOrder";
//            }
//
//            boolean requested = orderService.requestCancelOrder(orderId);
//            if (requested) {
//                redirectAttributes.addFlashAttribute("message", "주문 취소 요청이 접수되었습니다.");
//            } else {
//                redirectAttributes.addFlashAttribute("error", "주문 취소 요청에 실패했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("주문 취소 요청 실패 - orderId: {}", orderId, e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/orders/myOrder";
//    }
//
//    /**
//     * 반품 요청 (ORDERED → RETURN_REQUESTED)
//     */
//    @PostMapping("/{orderId}/request-return")
//    public String requestReturn(@PathVariable Long orderId,
//                                @AuthenticationPrincipal CustomUserDetails userDetails,
//                                RedirectAttributes redirectAttributes) {
//        try {
//            // 본인 주문인지 확인
//            OrderResponseDto order = orderService.getOrder(orderId);
//            if (!order.getMemberId().equals(userDetails.getMember().getId())) {
//                redirectAttributes.addFlashAttribute("error", "접근 권한이 없습니다.");
//                return "redirect:/orders/myOrder";
//            }
//
//            boolean requested = orderService.requestReturn(orderId);
//            if (requested) {
//                redirectAttributes.addFlashAttribute("message", "반품 요청이 접수되었습니다.");
//            } else {
//                redirectAttributes.addFlashAttribute("error", "반품 요청에 실패했습니다.");
//            }
//        } catch (Exception e) {
//            log.error("반품 요청 실패 - orderId: {}", orderId, e);
//            redirectAttributes.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/orders/myOrder";
//    }
}
