package zerobase.MyShoppingMall.repository.cart;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Cart;
import zerobase.MyShoppingMall.entity.Member;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMember(Member member);
    void deleteByMemberId(Long memberId);
    List<Long> findItemIdsByMemberId(Long memberId);
}
