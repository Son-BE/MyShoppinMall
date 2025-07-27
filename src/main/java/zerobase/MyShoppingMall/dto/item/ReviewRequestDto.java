package zerobase.MyShoppingMall.dto.item;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewRequestDto {
    private Long itemId;
    private Long orderId;
    private int rating;
    private String content;
}
