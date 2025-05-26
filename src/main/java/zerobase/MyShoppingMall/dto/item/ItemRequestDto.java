package zerobase.MyShoppingMall.dto.item;

import lombok.*;
//상품 등록, 수정 시 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequestDto {
    private String itemName;
    private String itemComment;
    private int price;
    private int quantity;
}
