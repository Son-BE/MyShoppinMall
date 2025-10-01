package zerobase.MyShoppingMall.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.dto.order.*;
import zerobase.MyShoppingMall.entity.*;
import zerobase.MyShoppingMall.repository.address.AddressRepository;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.repository.order.OrderRepository;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final AddressRepository addressRepository;
    private final CartService cartService;
    private final IamportService iamportService;
    private final ItemService itemService;

    /**
     * 주문 생성 (WAITING)
     * - 주소 오타 수정(receiverPhone)
     * - 서버 주도 금액 계산(요청 price 무시)
     * - 포인트 상한/검증
     * - 제로결제(remainingAmount == 0) 즉시 PAID 처리
     * - 장바구니 비우기 시점: PAID 이후
     */
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("기본주소는 필수 입력값");
        }

        Address address;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주소"));
        } else {
            address = Address.builder()
                    .receiverName(request.getRecipientName())
                    .receiverPhone(request.getRecipientPhone()) // ✅ 오타 수정
                    .postalCode(request.getPostalCode())
                    .addr(request.getAddressLine1())
                    .addrDetail(request.getAddressLine2())
                    .member(member)
                    .build();
            addressRepository.save(address);
        }

        // ✅ 서버 주도 금액 계산 (요청 DTO의 price는 신뢰하지 않음)
        int totalQuantity = 0;
        int finalAmount = 0;

        for (OrderDetailRequest d : request.getOrderDetails()) {
            Item item = itemRepository.findById(d.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없음: " + d.getItemId()));


            finalAmount += item.getPrice() * d.getQuantity();
            totalQuantity += d.getQuantity();
        }

        long usePoint = request.getUsePoint() != null ? request.getUsePoint() : 0L;
        if (usePoint < 0)
            throw new IllegalArgumentException("사용 포인트는 0포인트 이상이어야 합니다.");
        if (member.getPoint() < usePoint)
            throw new IllegalArgumentException("포인트가 부족합니다.");
        if (usePoint > finalAmount) usePoint = finalAmount; // ✅ 상한 적용

        int remainingAmount = (int) (finalAmount - usePoint);

        String merchantUid = "order_" + member.getId() + "_" + System.nanoTime();

        Order order = Order.builder()
                .member(member)
                .address(address)
                .merchantUid(merchantUid)
                .orderAddress(OrderAddress.builder()
                        .recipientName(request.getRecipientName())
                        .recipientPhone(request.getRecipientPhone())
                        .postalCode(request.getPostalCode())
                        .addressLine1(request.getAddressLine1())
                        .addressLine2(request.getAddressLine2())
                        .build())
                .paymentMethod(request.getPaymentMethod())
                .totalPrice(finalAmount)
                .totalAmount(totalQuantity)
                .usedPoint(usePoint)
                .orderStatus(OrderStatus.WAITING)
                .build();

        // 상세 생성 (서버 가격 고정)
        for (OrderDetailRequest d : request.getOrderDetails()) {
            Item item = itemRepository.findById(d.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없음: " + d.getItemId()));
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .item(item)
                    .price(item.getPrice()) // ✅ 서버 가격 사용
                    .quantity(d.getQuantity())
                    .build();
            order.addOrderDetail(detail);
        }

        // 포인트 차감 정책: 선차감 유지(대신 후속 실패 시 환급 경로 필수)
        if (usePoint > 0) {
            member.setPoint(member.getPoint() - usePoint);
            memberRepository.save(member);
        }

        Order savedOrder = orderRepository.save(order);

        // ✅ 제로결제: 즉시 PAID 전환 + 장바구니 비우기
        if (remainingAmount == 0) {
            savedOrder.setOrderStatus(OrderStatus.PAID);
            orderRepository.save(savedOrder);
            cartService.clearCart(member.getId());
        }

        return OrderCreateResponse.builder()
                .orderId(savedOrder.getId())
                .merchantUid(merchantUid)
                .remainingAmount(remainingAmount)
                .build();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없음"));

        List<OrderDetailResponse> detailDtos = order.getOrderDetails().stream()
                .map(detail -> new OrderDetailResponse(
                        detail.getItem().getItemName(),
                        detail.getPrice(),
                        detail.getQuantity(),
                        detail.getTotalPrice()
                ))
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberId(order.getMember().getId())  // ← 추가
                .memberName(order.getMember().getNickName())
                .receiverName(order.getOrderAddress().getRecipientName())
                .receiverPhone(order.getOrderAddress().getRecipientPhone())
                .receiverAddress(order.getOrderAddress().getAddressLine1())
                .receiverDetailAddress(order.getOrderAddress().getAddressLine2())
                .totalPrice(order.getTotalPrice())
                .usedPoint(order.getUsedPoint())  // ← 추가 (필요시)
                .itemNames(order.getOrderDetails().stream()
                        .map(detail -> detail.getItem().getItemName())
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt())
                .merchantUid(order.getMerchantUid())
                .orderStatus(order.getOrderStatus())
                .orderDetails(detailDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByMember(Long memberId) {
        return orderRepository.findByMember_Id(memberId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private OrderResponseDto mapToDto(Order order) {
        List<OrderDetailResponse> detailResponses = order.getOrderDetails().stream()
                .map(detail -> OrderDetailResponse.builder()
                        .itemId(detail.getItem().getId())
                        .itemName(detail.getItem().getItemName())
                        .price(detail.getPrice())
                        .quantity(detail.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberId(order.getMember().getId())  // ← 추가
                .memberName(order.getMember().getNickName())
                .receiverName(order.getOrderAddress().getRecipientName())
                .receiverPhone(order.getOrderAddress().getRecipientPhone())
                .receiverAddress(order.getOrderAddress().getAddressLine1())
                .receiverDetailAddress(order.getOrderAddress().getAddressLine2())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .totalPrice(order.getTotalPrice())
                .usedPoint(order.getUsedPoint())  // ← 추가 (필요시)
                .createdAt(order.getCreatedAt())
                .merchantUid(order.getMerchantUid())
                .orderStatus(order.getOrderStatus())
                .orderDetails(detailResponses)
                .build();
    }

    /**
     * 결제완료 처리(WAITING -> PAID)
     * - 멱등 처리(PAID면 true)
     * - 제로결제라면 verify 생략
     * - 장바구니 비우기 시점 이동
     */
    @Transactional
    public boolean markOrderCompleted(Long orderId, String impUid) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다."));

        if (order.getOrderStatus() == OrderStatus.PAID) {
            return true; // 멱등
        }
        if (order.getOrderStatus() != OrderStatus.WAITING) {
            log.warn("결제 처리 불가 상태 - 주문 ID: {}, 현재 상태: {}", orderId, order.getOrderStatus());
            return false;
        }

        int actualPaidAmount = (int) (order.getTotalPrice() - order.getUsedPoint());
        if (actualPaidAmount < 0) actualPaidAmount = 0;

        if (actualPaidAmount > 0) {
            boolean isValid = iamportService.verifyPayment(impUid, order.getMerchantUid(), actualPaidAmount);
            if (!isValid) {
                log.warn("결제 검증 실패 - impUid: {}, merchantUid: {}, 비교금액: {}", impUid, order.getMerchantUid(), actualPaidAmount);
                return false;
            }
            order.setImpUid(impUid);
        }

        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // ✅ 이제 장바구니 비우기
        cartService.clearCart(order.getMember().getId());

        log.info("결제 완료 처리 - 주문ID: {}, impUid: {}", orderId, impUid);
        return true;
    }

    /**
     * 주문확정 처리(PAID -> ORDERED)
     * - 재고 차감 시점을 확정 시점으로 선택한 경우 이 지점에서 차감 가능
     */
    @Transactional
    public boolean confirmOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalArgumentException("결제 완료된 주문만 확정할 수 있습니다.");
        }

        order.setOrderStatus(OrderStatus.ORDERED);
        orderRepository.save(order);

        // 주문 확정 시점에 상품 주문수 증가
        order.getOrderDetails().forEach(detail ->
                itemService.increaseOrderCount(detail.getItem().getId())
        );

        log.info("주문확정 완료 - 주문 ID: {}", orderId);
        return true;
    }

    /**
     * 즉시 주문 취소(WAITING OR PAID 상태에서만)
     * - 파라미터 impUid 제거 (주문의 impUid를 신뢰)
     * - 아임포트 응답 NPE 방지
     */
    @Transactional
    public boolean cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        if (order.getOrderStatus() != OrderStatus.WAITING && order.getOrderStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("취소 불가능한 주문 상태입니다.");
        }

        // 결제 전이면 바로 취소
        if (order.getOrderStatus() == OrderStatus.WAITING || order.getImpUid() == null || order.getImpUid().isBlank()) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            refundPointsToMember(order);
            orderRepository.save(order);
            log.info("주문 즉시 취소 완료 - 주문 ID: {}", orderId);
            return true;
        }

        // 결제 완료 상태면 환불 시도
        try {
            JsonNode result = iamportService.cancelPayment(order.getImpUid(), reason, null);
            JsonNode response = (result != null) ? result.get("response") : null;
            String status = (response != null && response.has("status")) ? response.get("status").asText() : null;

            if ("cancelled".equalsIgnoreCase(status)) {
                order.setOrderStatus(OrderStatus.CANCELLED);
                refundPointsToMember(order);
                orderRepository.save(order);
                log.info("결제 취소 완료 - 주문ID: {}", orderId);
                return true;
            } else {
                log.warn("결제 취소 실패 - 주문 ID: {}, 응답: {}", orderId, result);
                return false;
            }
        } catch (Exception e) {
            log.error("결제 취소 예외 - 주문 ID: {}", orderId, e);
            return false;
        }
    }

    /**
     * 주문 취소 요청(ORDERED 상태에서만)
     */
    @Transactional
    public boolean requestCancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.ORDERED) {
            throw new IllegalStateException("주문 완료 상태에서만 취소 요청이 가능합니다.");
        }

        order.setOrderStatus(OrderStatus.CANCEL_REQUESTED);
        orderRepository.save(order);

        log.info("주문 취소 요청 완료 - 주문 ID: {}", orderId);
        return true;
    }

    /**
     * 관리자의 취소 승인 처리(CANCEL_REQUESTED -> CANCEL_APPROVED -> CANCELLED)
     */
    @Transactional
    public boolean approveCancelRequest(Long orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.CANCEL_REQUESTED) {
            throw new IllegalArgumentException("취소 요청 상태의 주문만 승인할 수 있습니다.");
        }

        order.setOrderStatus(OrderStatus.CANCEL_APPROVED);
        orderRepository.save(order);

        // 실제 결제 취소 처리
        if (order.getImpUid() != null && !order.getImpUid().isBlank()) {
            try {
                JsonNode result = iamportService.cancelPayment(order.getImpUid(), "관리자 승인", null);
                JsonNode response = (result != null) ? result.get("response") : null;
                String status = (response != null && response.has("status")) ? response.get("status").asText() : null;

                if ("cancelled".equalsIgnoreCase(status)) {
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    refundPointsToMember(order);
                    orderRepository.save(order);
                    log.info("취소 승인 및 결제 취소 완료 - 주문 ID: {}, 관리자: {}", orderId, adminId);
                    return true;
                }
            } catch (Exception e) {
                log.error("결제 취소 예외 - 주문 ID: {}", orderId, e);
                return false;
            }
        } else {
            // 결제 정보가 없는 경우 바로 취소 처리
            order.setOrderStatus(OrderStatus.CANCELLED);
            refundPointsToMember(order);
            orderRepository.save(order);
            log.info("취소 승인 완료 - 주문 ID: {}, 관리자: {}", orderId, adminId);
            return true;
        }

        log.warn("취소 승인 처리 실패 - 주문ID: {}", orderId);
        return false;
    }

    /**
     * 취소 요청 거부(CANCEL_REQUESTED -> ORDERED)
     */
    @Transactional
    public boolean rejectCancelRequest(Long orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.CANCEL_REQUESTED) {
            throw new IllegalStateException("취소 요청 상태의 주문만 거부할 수 있습니다.");
        }
        order.setOrderStatus(OrderStatus.ORDERED);
        orderRepository.save(order);

        log.info("취소 요청 거부 - 주문 ID: {}, 관리자: {}", orderId, adminId);
        return true;
    }

    /**
     * 반품 요청 처리
     * - 기존: ORDERED → RETURNED (즉시 완료)
     * - 변경: ORDERED → RETURN_REQUESTED (요청 상태로 세분화)
     */
    @Transactional
    public boolean requestReturn(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.ORDERED) {
            throw new IllegalStateException("주문 완료된 상품만 반품 요청할 수 있습니다.");
        }

        order.setOrderStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);

        log.info("반품 요청 완료 - 주문 ID: {}", orderId);
        return true;
    }

    /**
     * 포인트 환급
     */
    private void refundPointsToMember(Order order) {
        if (order.getUsedPoint() > 0) {
            Member member = order.getMember();
            member.setPoint(member.getPoint() + order.getUsedPoint());
            memberRepository.save(member);
            log.info("포인트 환급 완료 - 회원 ID: {}, 환급 포인트: {}", member.getId(), order.getUsedPoint());
        }
    }

    @Transactional(readOnly = true)
    public Long findLatestOrderIdByMemberAndItem(Long memberId, Long itemId) {
        return orderRepository.findTopByMember_IdAndOrderDetails_Item_IdOrderByCreatedAtDesc(memberId, itemId)
                .map(Order::getId)
                .orElse(null);
    }

    /**
     * 반품 승인 처리
     */
    @Transactional
    public boolean approveReturnRequest(Long orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("반품 요청 상태의 주문만 승인할 수 있습니다.");
        }

        order.setOrderStatus(OrderStatus.RETURNED);
        refundPointsToMember(order);
        orderRepository.save(order);

        log.info("반품 승인 완료 - 주문 ID: {}, 관리자: {}", orderId, adminId);
        return true;

    }

    /**
     * 상태별 주문 목록 조회
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * 회원별 특정 상태 주문 조회
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByMemberAndStatus(Long memberId, OrderStatus status) {
        return orderRepository.findByMember_IdAndOrderStatus(memberId, status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * 반품 거부 처리 (RETURN_REQUESTED → ORDERED)
     */
    @Transactional
    public boolean rejectReturnRequest(Long orderId, String adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("반품 요청 상태의 주문만 거부할 수 있습니다.");
        }

        order.setOrderStatus(OrderStatus.ORDERED);
        orderRepository.save(order);

        log.info("반품 요청 거부 - 주문 ID: {}, 관리자: {}", orderId, adminId);
        return true;
    }
}
