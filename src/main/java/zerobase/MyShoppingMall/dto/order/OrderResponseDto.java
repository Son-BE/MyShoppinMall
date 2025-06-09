package zerobase.MyShoppingMall.dto.order;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId;
    private String memberName;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverDetailAddress;
    private String paymentMethod;
    private int totalPrice;
    private List<String> itemNames;
    private LocalDateTime orderDate;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailDto {
        private Long itemId;
        private String itemName;
        private int quantity;
        private int price;
    }
}
