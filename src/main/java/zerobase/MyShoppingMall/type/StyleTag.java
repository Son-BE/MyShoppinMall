package zerobase.MyShoppingMall.type;

public enum StyleTag {
    CASUAL("캐주얼"),
    FORMAL("포멀"),
    SPORTY("스포티"),
    STREET("스트릿"),
    VINTAGE("빈티지"),
    MINIMAL("미니멀"),
    ROMANTIC("로맨틱"),
    BUSINESS("비즈니스"),
    OUTDOOR("아웃도어"),
    TRENDY("트렌디");

    private final String displayName;

    StyleTag(String displayName) {
        this.displayName = displayName;
    }
}

