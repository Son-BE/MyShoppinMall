package zerobase.MyShoppingMall.type;

public enum Gender {
    MALE, FEMALE, UNISEX, OTHER;

    public String toLowerCase() {
        return this.name().toLowerCase();
    }
}
