package zerobase.MyShoppingMall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.repository.MemberRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public MemberResponseDto registerMember(MemberRequestDto memberRequestDto) {
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        Member member = Member.builder()
                .email(memberRequestDto.getEmail())
                .password(memberRequestDto.getPassword())
                .nickName(memberRequestDto.getNickName())
                .gender(memberRequestDto.getGender())
                .role(memberRequestDto.getRole())
                .phoneNumber(memberRequestDto.getPhoneNumber())
                .deleteType(memberRequestDto.getDeleteType())
                .createdAt(memberRequestDto.getCreatedAt())
                .updatedAt(memberRequestDto.getUpdatedAt())
                .build();

        Member savedMember = memberRepository.save(member);

        return MemberResponseDto.builder()
                .id(savedMember.getId())
                .email(savedMember.getEmail())
                .nickName(savedMember.getNickName())
                .gender(savedMember.getGender())
                .role(savedMember.getRole())
                .phoneNumber(savedMember.getPhoneNumber())
                .deleteType(savedMember.getDeleteType())
                .createdAt(savedMember.getCreatedAt())
                .updatedAt(savedMember.getUpdatedAt())
                .build();
    }

    public Optional<MemberResponseDto> findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> MemberResponseDto.builder()
                        .id(member.getId())
                        .email(member.getEmail())
                        .nickName(member.getNickName())
                        .gender(member.getGender())
                        .role(member.getRole())
                        .phoneNumber(member.getPhoneNumber())
                        .deleteType(member.getDeleteType())
                        .createdAt(member.getCreatedAt())
                        .updatedAt(member.getUpdatedAt())
                        .build());
    }

    public Optional<MemberResponseDto> findById(Long id) {
        return memberRepository.findById(id)
                .map(MemberResponseDto::new);
    }

    public void deleteById(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 ID의 회원이 존재하지 않습니다: " + id);
        }
        memberRepository.deleteById(id);
    }

}
