//package zerobase.MyShoppingMall.oAuth2;
//
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//
//import java.util.Map;
//
//public class OAuth2UserInfoFactory {
//    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
//        switch (registrationId.toLowerCase()) {
//            case "kakao":
//                return new KakaoOAuth2UserInfo(attributes);
//            case "naver":
//                return new NaverOAuth2UserInfo(attributes);
//            case "google":
//                return new GoogleOAuth2UserInfo(attributes);
//            default:
//                throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
//        }
//    }
//}
