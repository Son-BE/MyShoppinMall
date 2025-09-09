package zerobase.MyShoppingMall.utils.factory;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.Role;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class MemberDataFactory extends BaseDataFactory<Member> {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final List<String> domains = Arrays.asList(
            "gmail.com", "naver.com", "daum.net", "kakao.com", "yahoo.com", "outlook.com"
    );

    @Override
    public Member createSingle() {
        Gender gender = randomEnum(Gender.class);
        String firstName = gender == Gender.MALE ? faker.name().firstName() : faker.name().firstName();
        String lastName = faker.name().lastName();
        String fullName = lastName + firstName;

        String email = generateRealisticEmail(fullName);
        String password = passwordEncoder.encode("password123!"); // 기본 패스워드
        String phoneNumber = generateKoreanPhoneNumber();

        return Member.builder()
                .email(email)
                .password(password)
                .nickName(generateNickname(fullName))
                .phoneNumber(phoneNumber)
                .gender(gender)
                .role(random.nextDouble() < 0.95 ? Role.USER : Role.ADMIN) // 5% 확률로 관리자
                .createdAt(LocalDateTime.now().minusDays(random.nextInt(365)))
                .build();
    }

    @Override
    public Member createWithSpecificData(Object... params) {
        String email = (String) params[0];
        Role role = params.length > 1 ? (Role) params[1] : Role.USER;

        Member member = createSingle();
        member.setEmail(email);
        member.setRole(role);

        if (role == Role.ADMIN) {
            member.setNickName("관리자_" + member.getNickName());
        }

        return member;
    }

    private String generateRealisticEmail(String name) {
        String domain = domains.get(random.nextInt(domains.size()));
        String localPart = name.toLowerCase();

        // 다양한 이메일 패턴
        return switch (random.nextInt(4)) {
            case 0 -> localPart + random.nextInt(1000) + "@" + domain;
            case 1 -> localPart + "." + random.nextInt(100) + "@" + domain;
            case 2 ->
                    localPart + "_" + (1990 + random.nextInt(30)) + "@" + domain;
            default -> localPart + "@" + domain;
        };
    }

    private String generateKoreanPhoneNumber() {
        String[] prefixes = {"010", "011", "016", "017", "018", "019"};
        String prefix = prefixes[random.nextInt(prefixes.length)];

        int middle = 1000 + random.nextInt(9000);
        int last = 1000 + random.nextInt(9000);

        return String.format("%s-%04d-%04d", prefix, middle, last);
    }

    private String generateNickname(String realName) {
        List<String> adjectives = Arrays.asList(
                "행복한", "멋진", "귀여운", "시원한", "따뜻한", "빠른", "조용한", "활발한"
        );
        List<String> nouns = Arrays.asList(
                "고양이", "강아지", "토끼", "곰", "사자", "호랑이", "독수리", "늑대"
        );

        // 50% 확률로 실제 이름 사용, 50% 확률로 랜덤 닉네임
        if (random.nextBoolean()) {
            return realName;
        } else {
            String adjective = adjectives.get(random.nextInt(adjectives.size()));
            String noun = nouns.get(random.nextInt(nouns.size()));
            return adjective + noun + random.nextInt(1000);
        }
    }
}
