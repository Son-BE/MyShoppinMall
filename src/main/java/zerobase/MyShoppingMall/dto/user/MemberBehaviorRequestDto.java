package zerobase.MyShoppingMall.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import zerobase.MyShoppingMall.type.BehaviorType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberBehaviorRequestDto {

    private Long memberId;
    private Long itemId;
    private BehaviorType behaviorType;
}
