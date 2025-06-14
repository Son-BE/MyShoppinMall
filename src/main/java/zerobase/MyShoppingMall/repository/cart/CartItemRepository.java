package zerobase.MyShoppingMall.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import zerobase.MyShoppingMall.domain.Cart;
import zerobase.MyShoppingMall.domain.CartItem;
import zerobase.MyShoppingMall.domain.Item;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

}
