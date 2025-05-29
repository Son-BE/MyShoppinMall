package zerobase.MyShoppingMall.dto.Order;

import lombok.Getter;

@Getter
public class OrderCancelRequestDto {
    private Long orderId;
    private String reason;
}
