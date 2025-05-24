package zerobase.weather.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistAddRequestDTO {
    private Long userId;
    private Long productId;
}
