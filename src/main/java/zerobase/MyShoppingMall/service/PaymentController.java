package zerobase.MyShoppingMall.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import zerobase.MyShoppingMall.dto.payment.PaymentVerifyRequest;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final IamportService iamportService;

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerifyRequest request) {
        boolean verified = iamportService.verifyPayment(
                request.getImpUid(),
                request.getMerchantUid(),
                request.getAmount()
        );

        if (verified) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("결제 금액 검증 실패 또는 결제 실패");
        }
    }

    @PostMapping("/cancel")
    public JsonNode cancelPayment(@RequestBody Map<String, Object> request) {
        String impUid = (String) request.get("impUid");
        String reason = (String) request.getOrDefault("reason", "사용자 요청");
        Integer amount = request.containsKey("amount") ? (Integer) request.get("amount") : null;

        return iamportService.cancelPayment(impUid, reason, amount);
    }
}


//    @PostMapping("/verify")
//    public Mono<ResponseEntity<Map<String, Object>>> verifyPayment(
//            @RequestBody Map<String, String> request) {
//        String impUid = request.get("impUid");
//        String merchantUid = request.get("merchantUid");
//
//        if (impUid == null || merchantUid == null) {
//            return Mono.just(ResponseEntity.badRequest().body(
//                    Map.of("success", false, "message", "impUid 또는 merchantUid가 누락되었습니다.")
//            ));
//        }
//
//        return iamportService.verifyPayment(impUid).map(paymentInfo -> {
//            int paidAmount = paymentInfo.get("amount").asInt();
//            String status = paymentInfo.get("status").asText();
//            int expectedAmount = orderService.getOrderAmountByMerchantUid(merchantUid);
//
//            if (paidAmount == expectedAmount && "paid".equals(status)) {
//                orderService.markOrderAsPaid(merchantUid);
//                return ResponseEntity.ok(Map.of("success", true));
//            } else {
//                return ResponseEntity.ok(Map.of("success", false));
//            }
//        });
//    }


