package zerobase.MyShoppingMall.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    private String itemName;
    private String itemComment;
    private Integer price;
    private Integer quantity;

    private String category;
    private String subCategory;
    private String gender;
    private String season;
    private String style;
    private String primaryColor;
    private String secondaryColor;
    private String ageGroup;

    private String imagePath;
}