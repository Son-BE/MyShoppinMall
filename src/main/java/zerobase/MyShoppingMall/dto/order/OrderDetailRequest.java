package zerobase.MyShoppingMall.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class OrderDetailRequest {
    private Long itemId;
    private int quantity;
    private int price;
}
