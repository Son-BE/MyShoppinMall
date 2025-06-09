package zerobase.MyShoppingMall.dto.order;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {
    private Long itemId;
    private int price;
    private int quantity;

    public static OrderDetailResponse fromEntity(zerobase.MyShoppingMall.domain.OrderDetail detail) {
        return OrderDetailResponse.builder()
                .itemId(detail.getItem().getId())
                .price(detail.getPrice())
                .quantity(detail.getQuantity())
                .build();
    }
}

