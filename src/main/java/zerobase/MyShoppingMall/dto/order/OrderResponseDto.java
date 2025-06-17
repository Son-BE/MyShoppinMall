package zerobase.MyShoppingMall.dto.order;

import lombok.*;
import zerobase.MyShoppingMall.type.OrderStatus;

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
    private String memberName;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String receiverDetailAddress;
    private String paymentMethod;

    private int totalPrice;
    private List<String> itemNames;
    private LocalDateTime orderDate;
    private String merchantUid;
    private OrderStatus orderStatus;

    private List<OrderDetailResponse> orderDetails;

    public static OrderResponseDto from(zerobase.MyShoppingMall.domain.Order order) {
        List<String> itemNames = order.getOrderDetails().stream()
                .map(detail -> detail.getItem().getItemName())
                .collect(Collectors.toList());

        List<OrderDetailResponse> orderDetails = order.getOrderDetails().stream()
                .map(detail -> OrderDetailResponse.builder()
                        .itemId(detail.getItem().getId())
                        .itemName(detail.getItem().getItemName())
                        .price(detail.getPrice())
                        .quantity(detail.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberName(order.getMember().getNickName())
                .receiverName(order.getOrderAddress().getRecipientName())
                .receiverPhone(order.getOrderAddress().getRecipientPhone())
                .receiverAddress(order.getOrderAddress().getAddressLine1())
                .receiverDetailAddress(order.getOrderAddress().getAddressLine2())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .totalPrice(order.getTotalPrice())
                .itemNames(itemNames)
                .orderDate(order.getCreatedAt())
                .merchantUid(order.getMerchantUid())
                .orderStatus(order.getOrderStatus())
                .orderDetails(orderDetails)
                .build();
    }


}
