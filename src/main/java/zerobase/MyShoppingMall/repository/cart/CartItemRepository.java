package zerobase.MyShoppingMall.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Cart;
import zerobase.MyShoppingMall.entity.CartItem;
import zerobase.MyShoppingMall.entity.Item;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

}
