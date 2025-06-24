package zerobase.MyShoppingMall.Admin;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.entity.Order;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.repository.order.OrderRepository;
import zerobase.MyShoppingMall.service.IamportService;
import zerobase.MyShoppingMall.service.order.OrderStatsDto;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final IamportService iamportService;

    /**
     * 전체 주문 페이징 조회
     */
    @Transactional
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(OrderResponseDto::from);
    }

    /**
     * 상태별 주문 페이징 조회
     */
    @Transactional
    public Page<OrderResponseDto> getOrderDtosByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByOrderStatus(status, pageable)
                .map(OrderResponseDto::from);
    }

    /**
     * 주문 상세 조회
     */
    @Transactional
    public OrderResponseDto getOrderDetail(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));
        return OrderResponseDto.from(order);
    }

    /**
     * 주문 상태 변경
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다."));
        order.setOrderStatus(newStatus);
    }

    /**
     * 주문 통계 (총 주문 수, 상태별 수)
     */
    @Transactional
    public OrderStatsDto getOrderStats() {
        long total = orderRepository.count();

        Map<OrderStatus, Long> statusCounts = new EnumMap<>(OrderStatus.class);
        for (OrderStatus status : OrderStatus.values()) {
            long count = orderRepository.countByOrderStatus(status);
            statusCounts.put(status, count);
        }

        return OrderStatsDto.builder()
                .totalOrders(total)
                .statusCounts(statusCounts)
                .build();
    }

    @Transactional
    public boolean approveCancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.CANCEL_REQUESTED) {
            throw new IllegalStateException("취소 승인이 가능한 상태가 아닙니다.");
        }

        // 아임포트 결제 취소
        JsonNode result = iamportService.cancelPayment(order.getImpUid(), reason, null);
        String status = result.get("response").get("status").asText();

        if ("cancelled".equals(status)) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return true;
        }

        return false;
    }
}
