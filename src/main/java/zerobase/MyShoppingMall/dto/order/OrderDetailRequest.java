package zerobase.MyShoppingMall.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailRequest {
    private Long itemId;
    private int price;
    private int quantity;
    private int totalPrice;
}
