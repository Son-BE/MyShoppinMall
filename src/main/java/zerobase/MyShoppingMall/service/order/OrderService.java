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
import zerobase.MyShoppingMall.service.IamportService;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.type.OrderStatus;
import zerobase.MyShoppingMall.type.PaymentMethod;

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

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("기본 주소는 필수 입력값");
        }

        // 주소 처리 (기존 주소 사용 또는 신규 저장)
        Address address;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주소"));
        } else {
            address = Address.builder()
                    .receiverName(request.getRecipientName())
                    .receiverPhone(request.getRecipientPhone())
                    .postalCode(request.getPostalCode())
                    .addr(request.getAddressLine1())
                    .addrDetail(request.getAddressLine2())
                    .member(member)
                    .build();
            addressRepository.save(address);
        }

        // 총 결제금액 계산 (상품 가격 * 수량 합계)
        int finalAmount = request.getOrderDetails().stream()
                .mapToInt(detail -> detail.getPrice() * detail.getQuantity())
                .sum();
        int totalAmount = request.getOrderDetails().stream()
                .mapToInt(detail -> detail.getQuantity())
                .sum();


        // 사용 포인트: null일 경우 0으로 처리
        Long usePoint = request.getUsePoint() != null ? request.getUsePoint() : 0L;

        if (usePoint < 0) {
            throw new IllegalArgumentException("사용 포인트는 0 이상이어야 합니다.");
        }

        if (member.getPoint() < usePoint) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        // 포인트 사용 최대 한도는 총 결제금액
        if (usePoint > finalAmount) {
            usePoint = (long) finalAmount;
        }

        // 포인트 차감
        if (usePoint > 0) {
            member.setPoint(member.getPoint() - usePoint);
            memberRepository.save(member);
        }

        int remainingAmount = finalAmount - usePoint.intValue();

        // merchantUid 생성
        String merchantUid = "order_no_" + System.currentTimeMillis();

        // 주문 생성
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
                .totalAmount(totalAmount)
                .usedPoint((long) usePoint.intValue())  // 주문에 사용 포인트 기록
                .orderStatus(OrderStatus.WAITING)
                .build();

        // 주문 상세 생성
        for (OrderDetailRequest detailRequest : request.getOrderDetails()) {
            Item item = itemRepository.findById(detailRequest.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없음"));

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .item(item)
                    .price(detailRequest.getPrice())
                    .quantity(detailRequest.getQuantity())
                    .build();

            order.addOrderDetail(detail);
        }

        Order savedOrder = orderRepository.save(order);

        // 장바구니 비우기
        cartService.clearCart(member.getId());

        // 클라이언트에 주문 ID, merchantUid, 남은 결제금액 반환
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
                .memberName(order.getMember().getNickName())
                .receiverName(order.getOrderAddress().getRecipientName())
                .receiverPhone(order.getOrderAddress().getRecipientPhone())
                .receiverAddress(order.getOrderAddress().getAddressLine1())
                .receiverDetailAddress(order.getOrderAddress().getAddressLine2())
                .totalPrice(order.getTotalPrice())
                .itemNames(order.getOrderDetails().stream()
                        .map(detail -> detail.getItem().getItemName())
                        .collect(Collectors.toList()))
                .orderDate(order.getCreatedAt())
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
                .memberName(order.getMember().getNickName())
                .receiverName(order.getOrderAddress().getRecipientName())
                .receiverPhone(order.getOrderAddress().getRecipientPhone())
                .receiverAddress(order.getOrderAddress().getAddressLine1())
                .receiverDetailAddress(order.getOrderAddress().getAddressLine2())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .totalPrice(order.getTotalPrice())
                .orderDate(order.getCreatedAt())
                .merchantUid(order.getMerchantUid())
                .orderStatus(order.getOrderStatus())
                .orderDetails(detailResponses)
                .build();
    }

    @Transactional
    public boolean markOrderAsCompleted(Long orderId, String impUid) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다"));

        if (!order.getOrderStatus().equals(OrderStatus.WAITING)) {
            return false;
        }

        int actualPaidAmount = (int) (order.getTotalPrice() - order.getUsedPoint());
        boolean isValid = iamportService.verifyPayment(impUid, order.getMerchantUid(), actualPaidAmount);
        if (!isValid) {
            log.warn("결제 검증 실패 - impUid: {}, merchantUid: {}, 비교금액: {}", impUid, order.getMerchantUid(), actualPaidAmount);
            return false;
        }

        order.setImpUid(impUid);
        order.setOrderStatus(OrderStatus.ORDERED);
        orderRepository.save(order);

        return true;
    }

    @Transactional
    public boolean cancelOrder(Long orderId, String reason, String impUid) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        System.out.println("취소 요청 impUid: " + order.getImpUid());

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        if (impUid == null || impUid.isBlank()) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return true;
        }

        // 결제 취소 요청 (Iamport)
        JsonNode result = iamportService.cancelPayment(order.getImpUid(), reason, null);
        String status = result.get("response").get("status").asText();

        if ("cancelled".equals(status)) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            return true;
        }

        return false;
    }

    @Transactional
    public boolean requestCancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        if (order.getOrderStatus() != OrderStatus.ORDERED) {
            throw new IllegalStateException("취소 요청 가능한 상태가 아닙니다.");
        }

        order.setOrderStatus(OrderStatus.CANCEL_REQUESTED);
        orderRepository.save(order);
        return true;
    }

    //    @Transactional(readOnly = true)
//    public List<OrderResponseDto> getAllOrders() {
//        return orderRepository.findAll().stream()
//                .map(this::mapToDto)
//                .collect(Collectors.toList());
//    }

}

