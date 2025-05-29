package zerobase.MyShoppingMall.dto.Order;

import lombok.*;
import zerobase.MyShoppingMall.domain.OrderDetail;
import zerobase.MyShoppingMall.type.OrderStatus;
import zerobase.MyShoppingMall.type.PaymentMethod;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDto {
    private Long orderId;
    private Long memberId;
    private Long addressId;
    private int totalPrice;
    private OrderStatus status;
    private String reason;
    private PaymentMethod paymentMethod;
    private List<OrderDetailDto> orderDetails;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailDto {
        private Long itemId;
        private int price;
        private int quantity;
    }


}
