package zerobase.MyShoppingMall.repository.cart;


import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.MyShoppingMall.domain.Cart;
import zerobase.MyShoppingMall.domain.Member;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByMember(Member member);

    void deleteByMemberId(Long memberId);
}
