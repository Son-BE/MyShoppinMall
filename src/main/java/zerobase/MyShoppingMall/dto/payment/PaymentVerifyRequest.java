package zerobase.MyShoppingMall.dto.payment;

import lombok.Data;

@Data
public class PaymentVerifyRequest {
    private String impUid;
    private String merchantUid;
    private int amount;
}
