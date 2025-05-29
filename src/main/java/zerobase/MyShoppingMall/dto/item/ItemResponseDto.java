package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.type.Category;
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
    private Category category;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
    }

    public static ItemResponseDto fromEntity(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .itemComment(item.getItemComment())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .category(item.getCategory())
                .build();
    }
}
