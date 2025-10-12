package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.dto.payment.PaymentCompleteRequest;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.OrderService;
import zerobase.MyShoppingMall.temps.RecommendationService;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;
    private final RecommendationService recommendationService;


    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrderApi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody OrderCreateRequest request) {

        try {
            request.setMemberId(userDetails.getMember().getId());
            var createResponse = orderService.createOrder(request);
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", createResponse.getOrderId());
            response.put("merchantUid", createResponse.getMerchantUid());
            response.put("remainingAmount", createResponse.getRemainingAmount());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주문생성 실패", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        try {
            OrderResponseDto order = orderService.getOrder(orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            log.error("주문 조회 실패 - orderId: {}", orderId, e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/member")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long memberId = userDetails.getMember().getId();
            List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("회원 주문 목록 조회 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByMember(@PathVariable Long memberId) {
        try {
            List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("회원별 주문 목록 조회 실패 - memberId: {}", memberId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<OrderResponseDto> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("상태별 주문 목록 조회 실패 - status: {}", status, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/member/{memberId}/status/{status}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByMemberAndStatus(
            @PathVariable Long memberId,
            @PathVariable OrderStatus status) {
        try {
            List<OrderResponseDto> orders = orderService.getOrdersByMemberAndStatus(memberId, status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("회원별 상태별 주문 조회 실패 - memberId: {}, status: {}", memberId, status, e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<Map<String, Object>> completeOrder(@RequestBody PaymentCompleteRequest request) {
        try {
            boolean result = orderService.markOrderCompleted(request.getOrderId(), request.getImpUid());

            if (result) {
                recordPurchaseInteractions(request.getOrderId());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "결제 완료 처리 성공" : "결제 완료 처리 실패");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("결제 완료 처리 실패", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable Long orderId) {
        try {
            boolean result = orderService.confirmOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", "주문이 확정되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주문 확정 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long orderId,
                                                           @RequestBody(required = false) Map<String, String> requestBody) {
        try {
            String reason = requestBody != null ? requestBody.get("reason") : "사용자 요청";
            boolean result = orderService.cancelOrder(orderId, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "주문이 취소되었습니다." : "주문 취소에 실패했습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주문 취소 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/request-cancel")
    public ResponseEntity<Map<String, Object>> requestCancelOrder(@PathVariable Long orderId) {
        try {
            boolean result = orderService.requestCancelOrder(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", "주문 취소 요청이 접수되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("주문 취소 요청 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/approve-cancel")
    public ResponseEntity<Map<String, Object>> approveCancelRequest(@PathVariable Long orderId,
                                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String adminId = userDetails.getMember().getNickName();
            boolean result = orderService.approveCancelRequest(orderId, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "취소가 승인되었습니다." : "취소 승인에 실패했습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("취소 승인 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/reject-cancel")
    public ResponseEntity<Map<String, Object>> rejectCancelRequest(@PathVariable Long orderId,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String adminId = userDetails.getMember().getNickName();
            boolean result = orderService.rejectCancelRequest(orderId, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", "취소 요청이 거부되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("취소 거부 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }


    @PostMapping("/{orderId}/request-return")
    public ResponseEntity<Map<String, Object>> requestReturn(@PathVariable Long orderId) {
        try {
            boolean result = orderService.requestReturn(orderId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", "반품 요청이 접수되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 요청 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }


    @PostMapping("/{orderId}/approve-return")
    public ResponseEntity<Map<String, Object>> approveReturnRequest(@PathVariable Long orderId,
                                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String adminId = userDetails.getMember().getNickName();
            boolean result = orderService.approveReturnRequest(orderId, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", result ? "반품이 승인되었습니다." : "반품 승인에 실패했습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 승인 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/reject-return")
    public ResponseEntity<Map<String, Object>> rejectReturnRequest(@PathVariable Long orderId,
                                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            String adminId = userDetails.getMember().getNickName();
            boolean result = orderService.rejectReturnRequest(orderId, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result);
            response.put("message", "반품 요청이 거부되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("반품 거부 실패 - orderId: {}", orderId, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private void recordPurchaseInteractions(Long orderId) {
        try {
            OrderResponseDto order = orderService.getOrder(orderId);
            Long memberId = order.getMemberId();

            log.info("구매 상호작용 기록 시작 - 주문: {}, 사용자: {}", orderId, memberId);

        } catch (Exception e) {
            log.warn("구매 상호작용 기록 실패 - 주문: {}", orderId, e);
        }
    }

}





