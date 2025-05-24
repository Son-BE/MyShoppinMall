package zerobase.weather.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDTO {
    private Long orderId;
    private Long userId;
    private List<OrderItemDetailDTO> items;
    private String deliveryAddress;
    private String paymentMethod;
    private LocalDateTime orderDate;
    private String orderStatus;
    private int totalPrice;
}
