package zerobase.MyShoppingMall.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.dto.user.MemberUpdateDto;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.type.Role;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public MemberResponseDto registerMember(MemberRequestDto memberRequestDto) {
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }
        if (memberRepository.existsByNickName(memberRequestDto.getNickName())) {
            throw new IllegalStateException("이미 존재하는 닉네임입니다.");
        }

        Member member = Member.builder()
                .email(memberRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(memberRequestDto.getPassword()))
                .nickName(memberRequestDto.getNickName())
                .gender(memberRequestDto.getGender())
                .role(Role.USER)
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

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean existsByNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }

    public void deleteByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원이 존재하지 않습니다."));
        memberRepository.delete(member);
    }

    public long countMembers() {
        return memberRepository.count();
    }

    public MemberResponseDto getMemberProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다."));
        return new MemberResponseDto(member);
    }

    public void updateMemberProfile(Long memberId, MemberUpdateDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new UsernameNotFoundException("회원 정보를 찾을 수 없습니다."));
        member.setNickName(dto.getNickName());
        member.setPhoneNumber(dto.getPhoneNumber());
        memberRepository.save(member);
    }


    public MemberResponseDto getProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));
        return MemberResponseDto.fromEntity(member);
    }
}
