package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.type.*;

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
    private ItemCategory category;
    private ItemSubCategory subCategory;
    private Gender gender;
    private AgeGroup ageGroup;
    private Season season;
    private Styles style;
    private MultipartFile imageFile;
    private Color primaryColor;
    private Color secondaryColor;

}
