package zerobase.MyShoppingMall.dto.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCancelRequest {
    private String impUid;
    private String reason;
    private Integer amount;
}
