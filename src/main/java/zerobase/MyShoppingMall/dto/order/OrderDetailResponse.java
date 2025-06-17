package zerobase.MyShoppingMall.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private Long itemId;
    private int price;
    private int quantity;
    private String itemName;

    public OrderDetailResponse(String itemName, int price, int quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

}

