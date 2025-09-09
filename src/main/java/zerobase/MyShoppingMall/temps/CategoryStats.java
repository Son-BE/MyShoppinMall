package zerobase.MyShoppingMall.temps;

import java.util.List;

public class CategoryStats {
    private String chartData;
    private List<CategoryInfo> categories;

    // getters, setters
    public String getChartData() { return chartData; }
    public void setChartData(String chartData) { this.chartData = chartData; }
    public List<CategoryInfo> getCategories() { return categories; }
    public void setCategories(List<CategoryInfo> categories) { this.categories = categories; }
}
