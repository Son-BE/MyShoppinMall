package zerobase.MyShoppingMall.type;

public enum Season {
    SPRING,SUMMER,AUTUMN,WINTER,ALL_SEASON;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
