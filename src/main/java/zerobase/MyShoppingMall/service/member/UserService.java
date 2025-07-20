package zerobase.MyShoppingMall.service.member;

import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;

import java.util.Optional;

public interface UserService {
    MemberResponseDto registerMember(MemberRequestDto dto);
    Optional<MemberResponseDto> findByEmail(String email);
}
