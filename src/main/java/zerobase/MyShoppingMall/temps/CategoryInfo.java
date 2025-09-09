package zerobase.MyShoppingMall.temps;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryInfo {
    private String name;
    private int itemCount;
    private long viewCount;
    private double percentage;
    private String color;

    public CategoryInfo() {}

    public CategoryInfo(String name, int itemCount, long viewCount, double percentage, String color) {
        this.name = name;
        this.itemCount = itemCount;
        this.viewCount = viewCount;
        this.percentage = percentage;
        this.color = color;
    }

}
