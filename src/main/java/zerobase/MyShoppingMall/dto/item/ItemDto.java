package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;
import zerobase.MyShoppingMall.type.StyleTag;
import zerobase.MyShoppingMall.type.Color;

import java.util.Locale;
import java.util.Set;

@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;
    private String itemName;
    private Integer price;
    private Gender gender;
    private ItemCategory category;
    private ItemSubCategory subCategory;
    private String imagePath;
    private String description;
    private Integer quantity;
    private int rating;
    private Integer reviewCount;
    private Integer viewCount;
    private Integer orderCount;
    private Set<StyleTag> styleTags;
    private boolean isWish;
    private double matchScore;
    private String matchReason;

    private Color primaryColor;
    private Color secondaryColor;

    public static ItemDto from(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .gender(item.getGender())
                .category(item.getCategory())
                .imagePath(item.getImageUrl())
                .description(item.getItemComment())
                .quantity(item.getQuantity())
                .rating(item.getItemRating())
                .reviewCount(item.getReviewCount())
                .viewCount(item.getViewCount())
                .orderCount(item.getOrderCount())
                .styleTags(item.getStyleTags())
                .isWish(item.isWish())
                .primaryColor(item.getPrimaryColor())
                .secondaryColor(item.getSecondaryColor())
                .build();
    }
}
