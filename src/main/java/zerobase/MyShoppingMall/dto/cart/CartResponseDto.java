package zerobase.MyShoppingMall.dto.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

//장바구니 전체 응답
@Getter
@Setter
@Builder
public class CartResponseDto {
    private Long cartId;
    private Long memberId;
    private List<CartItemDto> items;
    private int totalPrice; // 장바구니 내 상품 가격 총합
}
