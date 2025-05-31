package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;
//상품 조회 시 반환
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ItemCategory category;
    private ItemSubCategory subCategory;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
        this.itemName = item.getItemName();
        this.itemComment = item.getItemComment();
        this.price = item.getPrice();
        this.quantity = item.getQuantity();
        this.deleteType = String.valueOf(item.getDeleteType());
        this.createdAt = item.getCreatedAt();
        this.updatedAt = item.getUpdatedAt();
        this.category = item.getCategory();
        this.subCategory = item.getSubCategory();
    }

    public static ItemResponseDto fromEntity(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .itemComment(item.getItemComment())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .category(item.getCategory())
                .subCategory(item.getSubCategory())
                .build();
    }
}
