package zerobase.MyShoppingMall.dto.user;

import lombok.*;
import zerobase.MyShoppingMall.domain.Member;

import java.time.LocalDate;

//클라이언트에게 회원정보를 응답할 때 사용
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {
    private Long id;
    private String email;
    private String nickName;
    private String gender;
    private String role;
    private String phoneNumber;
    private String deleteType;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public MemberResponseDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.nickName = member.getNickName();
        this.gender = member.getGender();
        this.role = member.getRole();
        this.phoneNumber = member.getPhoneNumber();
        this.deleteType = member.getDeleteType();
        this.createdAt = member.getCreatedAt();
        this.updatedAt = member.getUpdatedAt();
    }
}
