package zerobase.MyShoppingMall.type;

public enum AddToCartResult {
    ADDED,
    ALREADY_EXISTS,
    ;

    public boolean isSuccess() {
        return this == ADDED;
    }

    public String getMessage() {
        switch (this) {
            case ADDED:
                return "장바구니에 추가되었습니다.";
            case ALREADY_EXISTS:
                return "이미 장바구니에 등록된 상품입니다.";
            default:
                return "";
        }
    }
}
