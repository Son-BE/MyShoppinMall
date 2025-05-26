//package zerobase.weather.dto.cart;
//
//import lombok.*;
//import zerobase.weather.domain.CartItem;
//
//import java.math.BigDecimal;
//
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class CartItemResponseDTO {
//    private Long productId;
//    private String productName;
//    private int quantity;
//    private BigDecimal price;
//
//    public static CartItemResponseDTO fromEntity(CartItem cartItem) {
//        return CartItemResponseDTO.builder()
//                .productId(cartItem.getId())
//                .productName(cartItem.getProduct().getName())
//                .quantity(cartItem.getQuantity())
//                .price(cartItem.getProduct().getPrice())
//                .build();
//    }
//
//}
