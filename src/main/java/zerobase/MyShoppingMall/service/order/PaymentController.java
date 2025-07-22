package zerobase.MyShoppingMall.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.MyShoppingMall.dto.payment.PaymentVerifyRequest;

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





