package zerobase.MyShoppingMall.service.order;

import lombok.*;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatsDto {
    private long totalOrders;
    private Map<OrderStatus, Long> statusCounts;
}
