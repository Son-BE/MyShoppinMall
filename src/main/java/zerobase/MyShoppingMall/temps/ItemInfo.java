package zerobase.MyShoppingMall.temps;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemInfo {
    @JsonProperty("item_id")
    private int itemId;

    @JsonProperty("item_name")
    private String itemName;

    private double price;
    private String category;

    @JsonProperty("sub_category")
    private String subCategory;

    private String gender;
    private String style;

    @JsonProperty("primary_color")
    private String primaryColor;

    @JsonProperty("secondary_color")
    private String secondaryColor;

    @JsonProperty("item_rating")
    private double itemRating;

    @JsonProperty("popularity_score")
    private double popularityScore;

    @JsonProperty("recommendation_score")
    private double recommendationScore;

    @JsonProperty("recommendation_reason")
    private String recommendationReason;

    @JsonProperty("image_url")
    private String imageUrl;

    private String description;
}