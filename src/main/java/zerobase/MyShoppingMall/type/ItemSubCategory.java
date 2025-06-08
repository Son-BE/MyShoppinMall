package zerobase.MyShoppingMall.type;

public enum ItemSubCategory {
    //남성 상의
    M_HOODIE("후드티", ItemCategory.MENS_TOP),
    M_SWEATSHIRT("맨투맨", ItemCategory.MENS_TOP),
    M_TSHIRT("티셔츠", ItemCategory.MENS_TOP),
    M_WINDBREAKER("바람막이", ItemCategory.MENS_OUTER),
    M_COAT("코트", ItemCategory.MENS_OUTER),
    M_PADDING("패딩", ItemCategory.MENS_OUTER),


    //여성 상의
    W_HOODIE("후드티", ItemCategory.WOMENS_TOP),
    W_SWEATSHIRT("맨투맨", ItemCategory.WOMENS_TOP),
    W_TSHIRT("티셔츠", ItemCategory.WOMENS_TOP),
    W_COAT("코트", ItemCategory.WOMENS_OUTER),
    W_WINDBREAKER("바람막이", ItemCategory.WOMENS_OUTER),
    W_PADDING("패딩", ItemCategory.WOMENS_OUTER),

    //남성 하의
    M_JOGGER_PANTS("조거팬츠", ItemCategory.MENS_BOTTOM),
    M_TRAINING_PANTS("츄리닝", ItemCategory.MENS_BOTTOM),
    M_JEANS("청바지", ItemCategory.MENS_BOTTOM),

    //여성 하의
    W_JOGGER_PANTS("조거팬츠", ItemCategory.WOMENS_BOTTOM),
    W_TRAINING_PANTS("츄리닝", ItemCategory.WOMENS_BOTTOM),
    W_JEANS("청바지", ItemCategory.WOMENS_BOTTOM),
    W_SKIRTS("치마", ItemCategory.WOMENS_BOTTOM),

    //남성 신발
    M_SNEAKERS("스니커즈",ItemCategory.MENS_SHOES),
    M_RUNNING_SHOES("운동화",ItemCategory.MENS_SHOES),
    M_BOOTS("구두",ItemCategory.MENS_SHOES),

    //여성 신발
    W_SNEAKERS("스니커즈",ItemCategory.WOMENS_SHOES),
    W_RUNNING_SHOES("운동화",ItemCategory.WOMENS_SHOES),
    W_BOOTS("구두",ItemCategory.WOMENS_SHOES),

    //남성 악세서리
    M_WATCH("손목시계",ItemCategory.MENS_ACCESSORY),
    M_RING("반지",ItemCategory.MENS_ACCESSORY),
    M_NECKLACE("목걸이",ItemCategory.MENS_ACCESSORY),

    //여성 악세서리
    W_WATCH("손목시계",ItemCategory.WOMENS_ACCESSORY),
    W_RING("반지",ItemCategory.WOMENS_ACCESSORY),
    W_NECKLACE("목걸이",ItemCategory.WOMENS_ACCESSORY);



    private final String displayName;
    private final ItemCategory itemCategory;

    private ItemSubCategory(String displayName, ItemCategory itemCategory) {
        this.displayName = displayName;
        this.itemCategory = itemCategory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemCategory getItemCategory() {
        return itemCategory;
    }



}
