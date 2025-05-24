package zerobase.weather.dto.Order;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {
    private Long orderId;
    private List<OrderItemDTO> items;
    private String deliveryAddress;
    private String paymentMethod;
    private String orderStatus;
}
