package zerobase.MyShoppingMall.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//장바구니에 상품 추가 요청
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartAddItemRequestDto {
    private Long memberId;
    private Long itemId;
    private int quantity;
}
