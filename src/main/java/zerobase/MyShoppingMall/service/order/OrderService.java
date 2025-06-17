package zerobase.MyShoppingMall.service.order;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.*;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderDetailRequest;
import zerobase.MyShoppingMall.dto.order.OrderDetailResponse;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
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
    public Long createOrder(OrderCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (request.getAddressLine1() == null || request.getAddressLine1().isBlank()) {
            throw new IllegalArgumentException("기본 주소(addressLine1)는 필수 입력값입니다.");
        }

        //주소 저장 -> 영속 상태
        Address address;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주소입니다."));
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

        String merchantUid = "order_no_" + System.currentTimeMillis();
        int totalAmount = request.getOrderDetails().stream()
                .mapToInt(detail -> detail.getPrice() * detail.getQuantity())
                .sum();

        //주문 생성
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
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .totalPrice(request.getTotalPrice())
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.WAITING)
                .build();

        //주문 상세
        for (OrderDetailRequest detailRequest : request.getOrderDetails()) {
            Item item = itemRepository.findById(detailRequest.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .item(item)
                    .price(detailRequest.getPrice())
                    .quantity(detailRequest.getQuantity())
                    .build();
            order.addOrderDetail(detail);
        }
        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(member.getId());
        return savedOrder.getId();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        List<OrderDetailResponse> detailDtos = order.getOrderDetails().stream()
                .map(detail -> new OrderDetailResponse(
                        detail.getItem().getItemName(),
                        detail.getPrice(),
                        detail.getQuantity()
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

//    public int getOrderAmountByMerchantUid(String merchantUid) {
//        Order order = orderRepository.findByMerchantUid(merchantUid)
//                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + merchantUid));
//        return order.getTotalAmount();
//    }
//
//    public void markOrderAsPaid(String merchantUid) {
//        Order order = orderRepository.findByMerchantUid(merchantUid)
//                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
//        order.setOrderStatus(OrderStatus.PAID);
//        orderRepository.save(order);
//    }

    public boolean markOrderAsCompleted(Long orderId, String impUid) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다"));

        if (!order.getOrderStatus().equals(OrderStatus.WAITING)) {
            return false;
        }

        boolean isValid = iamportService.verifyPayment(impUid, order.getMerchantUid(), order.getTotalPrice());
        if (!isValid) {
            return false;
        }

        order.setImpUid(impUid);
        order.setOrderStatus(OrderStatus.ORDERED);
        orderRepository.save(order);

        return true;
    }

    @Transactional
    public boolean cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 주문입니다."));

        // 이미 취소된 주문이면 예외
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        // 결제 취소 (Iamport)
        JsonNode result = iamportService.cancelPayment(order.getImpUid(), reason, null);
        String status = result.get("response").get("status").asText();

        if ("cancelled".equals(status)) {
            order.setOrderStatus(OrderStatus.CANCELLED);
            return true;
        }

        return false;
    }


}
