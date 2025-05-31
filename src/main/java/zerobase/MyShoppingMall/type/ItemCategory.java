package zerobase.MyShoppingMall.type;

public enum ItemCategory {
    // 남성
    MENS_TOP("상의"),          // 남성 상의
    MENS_BOTTOM("하의"),       // 남성 하의
    MENS_OUTER("아우터"),        // 남성 아우터
    MENS_SHOES("신발"),        // 남성 신발
    MENS_ACCESSORY("악세서리"),    // 남성 악세서리

    // 여성
    WOMENS_TOP("상의"),        // 여성 상의
    WOMENS_BOTTOM("하의"),     // 여성 하의
    WOMENS_OUTER("아우터"),      // 여성 아우터
    WOMENS_SHOES("신발"),      // 여성 신발
    WOMENS_ACCESSORY("악세서리");  // 여성 악세서리

    private final String disPlayName;

    ItemCategory(String disPlayName) {
        this.disPlayName = disPlayName;
    }

    public String getDisplayName() {
        return disPlayName;
    }

}
