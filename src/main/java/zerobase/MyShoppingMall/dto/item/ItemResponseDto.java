package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import zerobase.MyShoppingMall.domain.Item;
//상품 조회 시 반환
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResponseDto {
    private Long id;
    private String itemName;
    private String itemComment;
    private int price;
    private int quantity;
    private String deleteType;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
    }
}
