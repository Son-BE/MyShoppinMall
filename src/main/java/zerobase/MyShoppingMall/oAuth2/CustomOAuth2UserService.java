package zerobase.MyShoppingMall.oAuth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.Role;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        System.out.println("OAuth2 Provider: " + userRequest.getClientRegistration().getRegistrationId());
        System.out.println("OAuth2 attributes: " + oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String email = null;
        String nickname = null;
        String phoneNumber = null;

        switch (registrationId.toLowerCase()) {
            case "kakao":
                email = extractKakaoEmail(oAuth2User);
                nickname = extractKakaoNickname(oAuth2User);
                break;
            case "naver":
                email = extractNaverEmail(oAuth2User);
                nickname = extractNaverNickname(oAuth2User);
                phoneNumber = extractPhoneNumber(oAuth2User);
                break;

            default:
                throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        }

        final String finalNickname = nickname;
        final String finalPhoneNumber = phoneNumber;
        String finalEmail1 = email;

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member m = new Member(finalEmail1);
                    m.setNickName(finalNickname != null ? finalNickname : "User_" + UUID.randomUUID().toString().substring(0, 8));
                    m.setPassword(UUID.randomUUID().toString());
                    m.setPhoneNumber(finalPhoneNumber);
//                    m.setGender(Gender.valueOf(userRequest.getClientRegistration().getRegistrationId()));
                    m.setRole(Role.USER);
                    return memberRepository.save(m);
                });

        return new CustomUserDetails(member, oAuth2User.getAttributes());
    }

    private String extractPhoneNumber(OAuth2User user) {

        String phoneNumber = (String) user.getAttributes().get("phoneNumber");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = UUID.randomUUID().toString().substring(0, 8);
        }
        return phoneNumber;
    }

    private String extractKakaoEmail(OAuth2User user) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) user.getAttributes().get("kakao_account");
        if (kakaoAccount == null) {
            throw new OAuth2AuthenticationException("카카오 계정 정보가 없습니다.");
        }
        String email = (String) kakaoAccount.get("email");
        if (email == null || email.isEmpty()) {

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            String nickname = profile != null ? (String) profile.get("nickname") : null;
            if (nickname == null || nickname.isEmpty()) {
                nickname = "kakaoUser_" + UUID.randomUUID().toString().substring(0, 8);
            }
            email = nickname + "@kakao.com";
        }
        return email;
    }

    private String extractKakaoNickname(OAuth2User user) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) user.getAttributes().get("kakao_account");
        if (kakaoAccount == null) {
            throw new OAuth2AuthenticationException("카카오 계정 정보가 없습니다.");
        }
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = profile != null ? (String) profile.get("nickname") : null;

        if (nickname == null || nickname.isEmpty()) {
            nickname = "kakaoUser_" + UUID.randomUUID().toString().substring(0, 8);
        }
        return nickname;
    }

    private String extractNaverEmail(OAuth2User user) {
        Map<String, Object> response = (Map<String, Object>) user.getAttributes().get("response");
        if (response == null) {
            throw new OAuth2AuthenticationException("네이버 계정 정보가 없습니다.");
        }
        String email = (String) response.get("email");
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("네이버 이메일 정보가 없거나 동의되지 않았습니다.");
        }
        return email;
    }

    private String extractNaverNickname(OAuth2User user) {
        String name = (String) user.getAttributes().get("name");
        if (name == null || name.isEmpty()) {
            name = "naverUser_" + UUID.randomUUID().toString().substring(0, 8);
        }
        return name;
    }
}
