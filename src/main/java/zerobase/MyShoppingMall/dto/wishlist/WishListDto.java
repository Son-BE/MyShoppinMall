package zerobase.MyShoppingMall.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishListDto {
    private Long id;
    private Long memberId;
    private Long itemId;
    private String itemName;
    private String itemImagePath;
    private int itemPrice;
    private boolean isWished;

    public boolean isWished() {
        return isWished;
    }

    public void setWished(boolean wished) {
        isWished = wished;
    }

    public String getFormattedPrice() {
        return String.format("₩" + "%,d", itemPrice);
    }
}
