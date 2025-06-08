package zerobase.MyShoppingMall.dto.order;

import lombok.*;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.domain.OrderDetail;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponseDto {
    private Long id;
    private Long itemId;
    private String itemName;
    private int price;
    private int quantity;

    public static OrderDetailResponseDto fromEntity(OrderDetail orderDetail) {
        Item item = orderDetail.getItem();

        return OrderDetailResponseDto.builder()
                .id(orderDetail.getId())
                .itemId(item.getId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .build();
    }
}


