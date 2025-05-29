package zerobase.MyShoppingMall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.Cart;
import zerobase.MyShoppingMall.domain.CartItem;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.dto.cart.CartItemDto;
import zerobase.MyShoppingMall.dto.cart.CartResponseDto;
import zerobase.MyShoppingMall.repository.CartItemRepository;
import zerobase.MyShoppingMall.repository.CartRepository;
import zerobase.MyShoppingMall.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // 장바구니 조회
    @Transactional(readOnly = true)
    public CartResponseDto getCartByCartId(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        List<CartItemDto> items = cart.getCartItems().stream()
                .map(cartItem -> CartItemDto.builder()
                        .itemId(cartItem.getId())
                        .itemId(cartItem.getItem().getId())
                        .itemName(cartItem.getItem().getItemName())
                        .price(cartItem.getItem().getPrice())
                        .quantity(cartItem.getQuantity())
                        .build())
                .toList();

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .memberId(cart.getMember().getId())
                .items(items)
                .build();
    }

    // 장바구니에 상품 추가
    @Transactional
    public void addItemToCart(Long cartId, Long itemId, int quantity) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        Optional<CartItem> existingCartItem = cartItemRepository.findByCartAndItem(cart, item);

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newCartItem);
        }
    }

    // 장바구니 상품 수량 수정
    @Transactional
    public void updateCartItemQuantity(Long cartItemId, int newQuantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));

        if (newQuantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
        }
    }

    // 장바구니 상품 삭제
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다."));
        cartItemRepository.delete(cartItem);
    }
}
