package zerobase.MyShoppingMall.repository.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Order;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMember_Id(Long memberId);
    Optional<Order> findTopByMember_IdAndOrderDetails_Item_IdOrderByCreatedAtDesc(Long memberId, Long itemId);
    long countByOrderStatus(OrderStatus orderStatus);

    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByMember_IdAndOrderStatus(Long memberId, OrderStatus orderStatus);

}