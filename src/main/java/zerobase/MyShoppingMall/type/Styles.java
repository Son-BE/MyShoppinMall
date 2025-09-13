package zerobase.MyShoppingMall.type;

public enum Styles {
    CASUAL,         // 캐주얼
    STREET,         // 스트리트
    DANDY,          // 댄디
    MINIMAL,        // 미니멀
    RETRO,          // 레트로
    LOVELY,         // 사랑스러운
    FORMAL,         // 포멀 / 정장
    SPORTY,         // 스포티
    UNIQUE,         // 개성있는
    CUTE,           // 귀여운
    CHIC,           // 시크
    CLEAN,          // 깔끔한
    SOFT,           // 부드러운
    VINTAGE, FRESH           // 산뜻한
    ;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}