package zerobase.MyShoppingMall.type;

public enum Season {
    SPRING,SUMMER,AUTUMN,WINTER;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
