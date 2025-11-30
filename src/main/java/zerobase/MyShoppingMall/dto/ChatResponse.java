package zerobase.MyShoppingMall.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatResponse {
    private String answer;
    private List<RelatedProduct> relatedProducts;

    @Getter
    @Setter
    public static class RelatedProduct {
        private Long productId;
        private String productName;
        private String category;
        private Double similarity;
    }
}
