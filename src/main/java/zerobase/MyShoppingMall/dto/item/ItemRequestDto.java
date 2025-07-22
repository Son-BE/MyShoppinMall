package zerobase.MyShoppingMall.dto.item;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

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
    private MultipartFile imageFile;


}
