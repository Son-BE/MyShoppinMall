package zerobase.MyShoppingMall.dto.payment;

import lombok.Data;

@Data
public class PaymentCompleteRequest {
    private Long orderId;
    private String impUid;
}
