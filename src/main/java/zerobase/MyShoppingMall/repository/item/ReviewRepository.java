package zerobase.MyShoppingMall.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.Order;
import zerobase.MyShoppingMall.entity.OrderDetail;
import zerobase.MyShoppingMall.entity.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByItemIdOrderByCreatedAtDesc(Long itemId);

    List<Review> findByItemId(Long itemId);

    boolean existsByItemIdAndMemberId(Long id, Long memberId);

    boolean existsByMemberIdAndItemIdAndOrderId(Long memberId, Long itemId, Long orderId);

    boolean existsByOrderDetail(OrderDetail orderDetail);

    List<Review> findByItem(Item item);
}
