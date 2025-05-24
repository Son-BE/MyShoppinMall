package zerobase.weather.dto.Order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDetailDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal pricePerItem;
    private BigDecimal totalItemPrice;

}
