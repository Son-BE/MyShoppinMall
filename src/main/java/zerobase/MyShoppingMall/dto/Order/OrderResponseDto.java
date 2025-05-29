package zerobase.MyShoppingMall.dto.Order;

import lombok.*;
import zerobase.MyShoppingMall.domain.Order;
import zerobase.MyShoppingMall.domain.OrderDetail;
import zerobase.MyShoppingMall.type.OrderStatus;
import zerobase.MyShoppingMall.type.PaymentMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId;
    private Long memberId;
    private Long addressId;
    private int totalPrice;
    private OrderStatus status;
    private String reason;
    private PaymentMethod paymentMethod;
    private LocalDate createdAt;
    private List<OrderDetailDto> orderDetails;

    public static OrderResponseDto fromEntity(Order order) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberId(order.getMember().getId())
                .addressId(order.getAddress().getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .reason(order.getReason())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .orderDetails(order.getOrderDetails().stream()
                        .map(OrderDetailDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetailDto {
        private Long itemId;
        private Long orderId;
        private int quantity;
        private int price;

        public static OrderDetailDto fromEntity(OrderDetail detail) {
            return OrderDetailDto.builder()
                    .itemId(detail.getItem().getId())
                    .orderId(detail.getItem().getId())
                    .price(detail.getPrice())
                    .quantity(detail.getQuantity())
                    .build();
        }
    }
}
