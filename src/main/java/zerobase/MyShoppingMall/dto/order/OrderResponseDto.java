package zerobase.MyShoppingMall.dto.order;

import lombok.*;
import zerobase.MyShoppingMall.domain.Order;
import zerobase.MyShoppingMall.domain.OrderAddress;
import zerobase.MyShoppingMall.type.OrderStatus;
import zerobase.MyShoppingMall.type.PaymentMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long id;
    private Long memberId;
    private Long addressId;
    private OrderAddress orderAddress;
    private int totalPrice;
    private OrderStatus status;
    private String reason;
    private PaymentMethod paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderDetailResponseDto> orderDetails;

    public static OrderResponseDto fromEntity(Order order) {

        return OrderResponseDto.builder()
                .id(order.getId())
                .memberId(order.getMember().getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .reason(order.getReason())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .orderDetails(
                        order.getOrderDetails().stream()
                                .map(OrderDetailResponseDto::fromEntity)
                                .collect(Collectors.toList())
                )
                .build();
    }
}

