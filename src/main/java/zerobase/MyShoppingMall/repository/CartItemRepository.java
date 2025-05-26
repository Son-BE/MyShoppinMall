//package zerobase.weather.repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import zerobase.weather.domain.CartItem;
//
//import java.util.List;
//
//public interface CartItemRepository extends JpaRepository<CartItem, Long> {
//    List<CartItem> findByMemberId(Long memberId);
//    void deleteByMemberIdAndProductId(Long memberId, Long productId);
//}
