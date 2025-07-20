package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.dto.payment.PaymentCompleteRequest;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderService orderService;

    // 주문 생성 API (JSON 요청/응답)
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrderApi(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @RequestBody OrderCreateRequest request) {
        request.setMemberId(userDetails.getMember().getId());
        Long orderId = orderService.createOrder(request).getOrderId();

        OrderResponseDto order = orderService.getOrder(orderId);
        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("merchantUid", order.getMerchantUid());

        return ResponseEntity.ok(response);
    }

    // 주문 단건 조회 API (JSON)
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        OrderResponseDto order = orderService.getOrder(orderId);
        return ResponseEntity.ok(order);
    }

    // 회원별 주문 목록 조회 API (JSON)
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByMember(@PathVariable Long memberId) {
        List<OrderResponseDto> orders = orderService.getOrdersByMember(memberId);
        return ResponseEntity.ok(orders);
    }

    // 결제 완료 처리 API (JSON)
    @PostMapping("/complete")
    public ResponseEntity<?> completeOrder(@RequestBody PaymentCompleteRequest request) {
        boolean result = orderService.markOrderAsCompleted(request.getOrderId(), request.getImpUid());

        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(500).body("주문 완료 처리 실패");
        }
    }
}
