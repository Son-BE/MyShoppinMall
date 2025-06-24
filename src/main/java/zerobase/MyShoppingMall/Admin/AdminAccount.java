package zerobase.MyShoppingMall.Admin;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.Role;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminAccount {
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AdminProperties adminProperties;

    @PostConstruct
    public void initAdmin() {
        if (!memberRepository.existsByEmail(adminProperties.getEmail())) {
            Member admin = Member.builder()
                    .email(adminProperties.getEmail())
                    .password(bCryptPasswordEncoder.encode(adminProperties.getPassword()))
                    .nickName("관리자")
                    .gender(Gender.MALE)
                    .role(Role.ADMIN)
                    .phoneNumber("010-5037-3271")
                    .deleteType("F")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            memberRepository.save(admin);
        }
    }
}
