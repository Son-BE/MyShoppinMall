package zerobase.MyShoppingMall.dto.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.type.*;

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
    private AgeGroup ageGroup;
    private Styles style;
    private Season season;
    private boolean isWish;

    private Color primaryColor;
    private Color secondaryColor;

    private String recommendationReason;    // 추천 사유
    private Double recommendationScore;     // 추천 점수
    private Double popularityScore;         // 인기 점수

    // 새로 추가
    private Double itemRating;              // 상품 평점
    private Integer reviewCount;            // 리뷰 개수

    @Builder
    public ItemResponseDto(Long id, String itemName, String itemComment, int price, int quantity, String deleteType,
                           String imagePath, LocalDateTime createdAt, LocalDateTime updatedAt,
                           ItemCategory category, ItemSubCategory subCategory, Gender gender,
                           AgeGroup ageGroup, Styles style, Season season, boolean isWish,
                           Color primaryColor, Color secondaryColor,
                           String recommendationReason, Double recommendationScore, Double popularityScore,
                           Double itemRating, Integer reviewCount) {
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
        this.ageGroup = ageGroup;
        this.style = style;
        this.season = season;
        this.isWish = isWish;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.recommendationReason = recommendationReason;
        this.recommendationScore = recommendationScore;
        this.popularityScore = popularityScore;
        this.itemRating = itemRating;
        this.reviewCount = reviewCount;
    }

    public static ItemResponseDto fromEntity(Item item) {
        return ItemResponseDto.builder()
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
                .ageGroup(item.getAgeGroup())
                .style(item.getStyle())
                .season(item.getSeason())
                .imagePath(item.getImageUrl())
                .isWish(item.isWish())
                .primaryColor(item.getPrimaryColor())
                .secondaryColor(item.getSecondaryColor())
                .itemRating((double) item.getItemRating())
                .reviewCount(item.getReviewCount())
                .build();
    }

    public String getImageUrl() {
        return imagePath;
    }

    public String getFormattedPrice() {
        return String.format("%,d", price);
    }

    public void setIsWish(boolean isWish) {
        this.isWish = isWish;
    }

    public Long getItemId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public String getRecommendationReason() {
        return recommendationReason != null ? recommendationReason : "추천 상품";
    }

    public Double getRecommendationScore() {
        return recommendationScore != null ? recommendationScore : 0.0;
    }

    public Double getPopularityScore() {
        return popularityScore != null ? popularityScore : 0.0;
    }
    public void setImageUrl(String imageUrl) {
        this.imagePath = imageUrl;
    }
}
