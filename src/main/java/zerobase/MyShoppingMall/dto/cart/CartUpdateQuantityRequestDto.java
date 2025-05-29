package zerobase.MyShoppingMall.dto.cart;

import lombok.Getter;
import lombok.Setter;

//장바구니 내 수량 요청 변경
@Getter
@Setter
public class CartUpdateQuantityRequestDto {
    private Long cartItemId;
    private int newQuantity;
}
