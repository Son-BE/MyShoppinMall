package zerobase.MyShoppingMall.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemResponseDto {
    private Long cartItemId;
    private Long itemId;
    private String itemName;
    private int quantity;
    private int price;
    private String imagePath;
    private ItemResponseDto item;


}
