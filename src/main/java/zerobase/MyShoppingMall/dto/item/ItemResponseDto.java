package zerobase.MyShoppingMall.dto.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemResponseDto {
    private Long id;
    private String itemName;
    private String itemComment;
    private int price;
    private int quantity;
    private String deleteType;
    private String imagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ItemCategory category;
    private ItemSubCategory subCategory;
    private Gender gender;
    private boolean isWish;

    public ItemResponseDto(Long id, String itemName, String itemComment, int price, int quantity, String deleteType,
                           String imagePath, LocalDateTime createdAt, LocalDateTime updatedAt,
                           ItemCategory category, ItemSubCategory subCategory, Gender gender, boolean isWish) {
        this.id = id;
        this.itemName = itemName;
        this.itemComment = itemComment;
        this.price = price;
        this.quantity = quantity;
        this.deleteType = deleteType;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.category = category;
        this.subCategory = subCategory;
        this.gender = gender;
        this.isWish = isWish;
    }

    public static ItemResponseDto fromEntity(Item item) {

        ItemResponseDto dto = ItemResponseDto.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .itemComment(item.getItemComment())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .deleteType(String.valueOf(item.getDeleteType()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .category(item.getCategory())
                .subCategory(item.getSubCategory())
                .gender(item.getGender())
                .imagePath(item.getImageUrl())
                .isWish(item.isWish())
                .build();

        return dto;
    }

    public String getImageUrl() {
        return imagePath;
    }

    public String getFormattedPrice() {
        return String.format("%,d", price);
    }

    public void setIsWish(boolean isWish) {
    }
}
