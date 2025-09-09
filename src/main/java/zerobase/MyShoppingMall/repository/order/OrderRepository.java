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
    // ========== 페이징 처리 메서드들 (AdminOrderController용) ==========

    /**
     * 특정 상태의 주문 조회 (페이징)
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    /**
     * 특정 회원의 특정 상태 주문 조회 (페이징)
     */
    Page<Order> findByMember_IdAndOrderStatus(Long memberId, OrderStatus orderStatus, Pageable pageable);

    /**
     * 모든 주문 조회 (페이징) - 이미 JpaRepository에서 제공하지만 명시적으로 표기
     */
    // Page<Order> findAll(Pageable pageable); // 이미 JpaRepository에서 제공

    // ========== 리스트 조회 메서드들 (OrderMvcController용) ==========

    /**
     * 특정 상태의 모든 주문 조회 (리스트)
     */
    List<Order> findByOrderStatus(OrderStatus orderStatus);

    /**
     * 특정 회원의 특정 상태 주문 조회 (리스트)
     */
    List<Order> findByMember_IdAndOrderStatus(Long memberId, OrderStatus orderStatus);

    /**
     * 특정 상태의 주문을 생성일자 기준 내림차순으로 조회
     */
    List<Order> findByOrderStatusOrderByCreatedAtDesc(OrderStatus orderStatus);

    /**
     * 특정 회원의 특정 상태 주문을 생성일자 기준 내림차순으로 조회
     */
    List<Order> findByMember_IdAndOrderStatusOrderByCreatedAtDesc(Long memberId, OrderStatus orderStatus);

    /**
     * 여러 상태의 주문 조회 (IN 쿼리)
     */
    List<Order> findByOrderStatusIn(List<OrderStatus> orderStatuses);

    /**
     * 특정 회원의 여러 상태 주문 조회 (IN 쿼리)
     */
    List<Order> findByMember_IdAndOrderStatusIn(Long memberId, List<OrderStatus> orderStatuses);
}