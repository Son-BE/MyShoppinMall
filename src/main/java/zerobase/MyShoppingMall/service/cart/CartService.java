package zerobase.MyShoppingMall.service.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import zerobase.MyShoppingMall.entity.Cart;
import zerobase.MyShoppingMall.entity.CartItem;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.cart.CartItemRepository;
import zerobase.MyShoppingMall.repository.cart.CartRepository;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.AddToCartResult;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

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
    public AddToCartResult addItemToCart(Long memberId, Long itemId, int quantity) {
        if(quantity <= 0) throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");

        Cart cart = getOrCreateCart(memberId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        CartItem existingCartItem = cartItemRepository.findByCartAndItem(cart, item).orElse(null);

        if (existingCartItem != null) {
            return AddToCartResult.ALREADY_EXISTS;
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .item(item)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newCartItem);
            cart.getCartItems().add(newCartItem);
        }
        return AddToCartResult.ADDED;
    }
    @Transactional()
    public List<CartItem> getCartItems(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        return cart.getCartItems();
    }
    public void updateItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템이 없습니다."));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
    }
    @Transactional
    public void clearCart(Long memberId) {
        Cart cart = getOrCreateCart(memberId);
        for(CartItem item : cart.getCartItems()) {
            item.setCart(null);
        }
        cartItemRepository.deleteAll(cart.getCartItems());
        cart.getCartItems().clear();

    }

    // 특정 장바구니 아이템 삭제
    public void deleteCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }


}
