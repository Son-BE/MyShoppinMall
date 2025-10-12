package zerobase.MyShoppingMall.oAuth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.Role;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {
    private final MemberRepository memberRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = new OidcUserService().loadUser(userRequest);

        System.out.println("OAuth2 Provider: google (OIDC)");
        System.out.println("OIDC attributes: " + oidcUser.getAttributes());

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String gender;
        String phone;

        Map<String, Object> attributes = oidcUser.getAttributes();
        if (attributes.containsKey("gender")) {
            gender = (String) attributes.get("gender");
        } else {
            gender = null;
        }
        if (attributes.containsKey("phone_number")) {
            phone = (String) attributes.get("phone_number");
        } else {

            phone = null;
        }

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member m = new Member(email);
                    m.setNickName(name != null ? name : "googleUser_" + UUID.randomUUID().toString().substring(0, 8));
                    m.setPassword(UUID.randomUUID().toString());

                    try {
                        m.setGender(gender != null ? Gender.valueOf(gender.toUpperCase()) : Gender.MALE);
                    } catch (IllegalArgumentException e) {
                        m.setGender(Gender.MALE);
                    }

                    m.setPhoneNumber(phone != null ? phone : "000-0000-0000");

                    m.setRole(Role.USER);
                    return memberRepository.save(m);
                });

        return new CustomUserDetails(member, oidcUser.getAttributes());
    }
}
