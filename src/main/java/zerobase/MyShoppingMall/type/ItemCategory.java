package zerobase.MyShoppingMall.type;

public enum ItemCategory {
    // 남성
    MENS_TOP("남성 상의",Gender.MALE),          // 남성 상의
    MENS_BOTTOM("남성 하의",Gender.MALE),       // 남성 하의
    MENS_OUTER("남성 아우터",Gender.MALE),        // 남성 아우터
    MENS_SHOES("남성 신발",Gender.MALE),        // 남성 신발
    MENS_ACCESSORY("남성 악세서리",Gender.MALE),    // 남성 악세서리

    // 여성
    WOMENS_TOP("여성 상의",Gender.FEMALE),        // 여성 상의
    WOMENS_BOTTOM("여성 하의",Gender.FEMALE),     // 여성 하의
    WOMENS_OUTER("여성 아우터",Gender.FEMALE),      // 여성 아우터
    WOMENS_SHOES("여성 신발",Gender.FEMALE),      // 여성 신발
    WOMENS_ACCESSORY("여성 악세서리",Gender.FEMALE);  // 여성 악세서리

    private final String disPlayName;
    private final Gender gender;

    ItemCategory(String disPlayName,Gender gender) {
        this.disPlayName = disPlayName;
        this.gender = gender;
    }

    public String getDisplayName() {
        return disPlayName;
    }

    public Gender getGender() {
        return gender;
    }
}
