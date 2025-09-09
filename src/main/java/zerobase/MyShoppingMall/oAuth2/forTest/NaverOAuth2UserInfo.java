//package zerobase.MyShoppingMall.oAuth2;
//
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//
//import java.util.Map;
//import java.util.UUID;
//
//public class NaverOAuth2UserInfo implements OAuth2UserInfo {
//    private final Map<String, Object> attributes;
//    private final Map<String, Object> response;
//
//    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
//        this.attributes = attributes;
//        this.response = (Map<String, Object>) attributes.get("response");
//    }
//
//    @Override
//    public String getEmail() {
//        String email = response != null ? (String) response.get("email") : null;
//        if (email == null || email.isEmpty()) {
//            throw new OAuth2AuthenticationException("네이버 이메일 정보가 없거나 동의되지 않았습니다.");
//        }
//        return email;
//    }
//
//    @Override
//    public String getNickname() {
//        String name = response != null ? (String) response.get("name") : null;
//        String nickname = response != null ? (String) response.get("nickname") : null;
//
//        String result = nickname != null ? nickname : name;
//        return result != null ? result : "naverUser_" + UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    @Override
//    public String getPhoneNumber() {
//        String mobile = response != null ? (String) response.get("mobile") : null;
//        return mobile != null ? mobile : "010-0000-0000";
//    }
//
//    @Override
//    public String getGender() {
//        return response != null ? (String) response.get("gender") : null;
//    }
//
//    @Override
//    public String getProvider() {
//        return "naver";
//    }
//}
