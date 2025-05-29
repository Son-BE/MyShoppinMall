package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import zerobase.MyShoppingMall.type.Category;

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
    private Category category;
}
