package zerobase.MyShoppingMall.dto.item;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemPointGrantRequest {
    private Long memberId;
    private Long points;
}
