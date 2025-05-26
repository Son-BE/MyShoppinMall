package zerobase.MyShoppingMall.dto.user;

import lombok.*;

import java.time.LocalDate;

// 회원가입 시 클라이언트로부터 전달받을 데이터를 담음
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRequestDto {
    private String email;
    private String password;
    private String nickName;
    private String gender;
    private String role;
    private String phoneNumber;
    private String deleteType;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
