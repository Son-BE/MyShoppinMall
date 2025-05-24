package zerobase.weather.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.weather.domain.CartItem;
import zerobase.weather.domain.Product;
import zerobase.weather.dto.cart.CartItemAddRequestDTO;
import zerobase.weather.dto.cart.CartItemResponseDTO;
import zerobase.weather.repository.CartItemRepository;
import zerobase.weather.repository.ProductRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public void addToCart(CartItemAddRequestDTO requestDTO, Long memberId) {
        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다!"));

        CartItem cartItem = CartItem.builder()
                .product(product)
                .memberId(memberId)
                .quantity(requestDTO.getQuantity())
                .build();

        cartItemRepository.save(cartItem);
    }

    public List<CartItemResponseDTO> getCartItems(Long memberId) {
        return cartItemRepository.findByMemberId(memberId).stream()
                .map(CartItemResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public void removeFromCart(Long memberId, Long productId) {
        cartItemRepository.deleteByMemberIdAndProductId(memberId, productId);
    }
}
