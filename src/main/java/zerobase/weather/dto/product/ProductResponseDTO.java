package zerobase.weather.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponseDTO {

    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private int stock;

    private String imageUrl;
}
