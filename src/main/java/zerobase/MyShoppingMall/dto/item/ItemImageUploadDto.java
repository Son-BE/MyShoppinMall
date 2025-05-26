package zerobase.MyShoppingMall.dto.item;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ItemImageUploadDto {
    private Long itemId;
    private MultipartFile imageFile;
}
