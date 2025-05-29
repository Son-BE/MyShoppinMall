package zerobase.MyShoppingMall.dto.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CartItemDto {
    private Long itemId;
    private String itemName;
    private int quantity;
    private int price;
    private int totalPrice; // q * p
}
