package zerobase.MyShoppingMall.dto.user;

import lombok.*;
import zerobase.MyShoppingMall.type.Gender;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberUpdateDto {

    private String nickName;
    private Gender gender;
    private String phoneNumber;
}
