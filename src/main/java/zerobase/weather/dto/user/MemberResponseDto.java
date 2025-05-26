package zerobase.weather.dto.user;

import lombok.*;

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
}
