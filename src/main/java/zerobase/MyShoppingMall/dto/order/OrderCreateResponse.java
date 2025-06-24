package zerobase.MyShoppingMall.dto.order;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateResponse {
    private Long orderId;
    private String merchantUid;
    private int remainingAmount;
}
