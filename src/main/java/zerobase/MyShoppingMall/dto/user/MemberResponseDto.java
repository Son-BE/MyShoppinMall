package zerobase.MyShoppingMall.dto.user;

import lombok.*;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.Role;

import java.time.LocalDateTime;

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
    private Gender gender;
    private Role role;
    private String phoneNumber;
    private String deleteType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Address defaultAddress;
    private Long point;

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
        this.point = member.getPoint();
        this.defaultAddress = member.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);

    }

    public static MemberResponseDto fromEntity(Member member) {
        Address defaultAddress = member.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);

        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickName(member.getNickName())
                .phoneNumber(member.getPhoneNumber())
                .createdAt(member.getCreatedAt())
                .defaultAddress(defaultAddress)
                .point(member.getPoint())
                .build();
    }
}
