//package zerobase.MyShoppingMall.oAuth2;
//
//import java.util.Map;
//import java.util.UUID;
//
//public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
//    private final Map<String, Object> attributes;
//    private final Map<String, Object> kakaoAccount;
//    private final Map<String, Object> profile;
//
//    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
//        this.attributes = attributes;
//        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
//        this.profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
//    }
//
//    @Override
//    public String getEmail() {
//        String email = kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
//        if (email == null || email.isEmpty()) {
//            String nickname = getNickname();
//            email = nickname + "@kakao.temp";
//        }
//        return email;
//    }
//
//    @Override
//    public String getNickname() {
//        String nickname = profile != null ? (String) profile.get("nickname") : null;
//        return nickname != null ? nickname : "kakaoUser_" + UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    @Override
//    public String getPhoneNumber() {
//        return "010-0000-0000"; // 카카오는 전화번호 제공 x
//    }
//
//    @Override
//    public String getGender() {
//        return kakaoAccount != null ? (String) kakaoAccount.get("gender") : null;
//    }
//
//    @Override
//    public String getProvider() {
//        return "kakao";
//    }
//}
