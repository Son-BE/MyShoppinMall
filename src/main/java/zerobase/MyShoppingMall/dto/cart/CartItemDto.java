package zerobase.MyShoppingMall.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemDto {
    private Long cartItemId;
    private Long itemId;
    private String itemName;
    private int quantity;
    private int price;
    private int totalPrice;
}
