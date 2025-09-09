package zerobase.MyShoppingMall.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Cart;
import zerobase.MyShoppingMall.entity.CartItem;
import zerobase.MyShoppingMall.entity.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    @Query("SELECT ci.item.id FROM CartItem ci WHERE ci.cart.member.id = :memberId")
    List<Long> findItemIdsByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.item.id = :itemId")
    void deleteByItemId(@Param("itemId") Long itemId);

}
