package zerobase.MyShoppingMall.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zerobase.MyShoppingMall.domain.Cart;
import zerobase.MyShoppingMall.domain.CartItem;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.repository.cart.CartItemRepository;
import zerobase.MyShoppingMall.repository.cart.CartRepository;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    //회원의 장바구니 생성 or 조회
    public Cart getOrCreateCart(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new IllegalArgumentException("회원이 없습니다."));

        return cartRepository.findByMember(member)
                .orElseGet(() -> {
                    Cart cart = Cart.builder()
                            .member(member)
                            .build();
                    return cartRepository.save(cart);
                });
    }

    //장바구니에 상품 추가
    public void addItemToCart(Long memberId, Long itemId, int quantity) {
        if(quantity <= 0) throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");

        Cart cart = getOrCreateCart(memberId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        CartItem existingCartItem = cartItemRepository.findByCartAndItem(cart, item).orElse(null);

        if (existingCartItem != null) {
            existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newCartItem);
            cart.getCartItems().add(newCartItem);
        }
    }


    // 회원의 장바구니 아이템 리스트 조회
    @Transactional()
    public List<CartItem> getCartItems(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        return cart.getCartItems();
    }

    // 장바구니 아이템 수량 변경
    public void updateItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템이 없습니다."));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }

    // 장바구니 비우기
    public void clearCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

    }

    // 특정 장바구니 아이템 삭제
    public void deleteCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

}
