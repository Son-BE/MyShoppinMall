//package zerobase.MyShoppingMall.oAuth2;
//
//import java.util.Map;
//import java.util.UUID;
//
//public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
//    private final Map<String, Object> attributes;
//
//    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
//        this.attributes = attributes;
//    }
//
//    @Override
//    public String getEmail() {
//        return (String) attributes.get("email");
//    }
//
//    @Override
//    public String getNickname() {
//        String name = (String) attributes.get("name");
//        return name != null ? name : "googleUser_" + UUID.randomUUID().toString().substring(0, 8);
//    }
//
//    @Override
//    public String getPhoneNumber() {
//        String phone = (String) attributes.get("phone_number");
//        return phone != null ? phone : "010-0000-0000";
//    }
//
//    @Override
//    public String getGender() {
//        return (String) attributes.get("gender");
//    }
//
//    @Override
//    public String getProvider() {
//        return "google";
//    }
//}
