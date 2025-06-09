package zerobase.MyShoppingMall.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.*;
import zerobase.MyShoppingMall.dto.order.OrderCreateRequest;
import zerobase.MyShoppingMall.dto.order.OrderDetailRequest;
import zerobase.MyShoppingMall.dto.order.OrderResponseDto;
import zerobase.MyShoppingMall.repository.address.AddressRepository;
import zerobase.MyShoppingMall.repository.cart.CartRepository;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.repository.order.OrderRepository;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.type.PaymentMethod;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;

    @Transactional
    public Long createOrder(OrderCreateRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        //주소 저장 -> 영속 상태
        Address address = Address.builder()
                .receiverName(request.getRecipientName())
                .receiverPhone(request.getRecipientPhone())
                .postalCode(request.getPostalCode())
                .addr(request.getAddressLine1())
                .addrDetail(request.getAddressLine2())
                .member(member)
                .build();

        addressRepository.save(address);

        //주문 생성
        Order order = Order.builder()
                .member(member)
                .address(address)
                .orderAddress(OrderAddress.builder()
                        .recipientName(request.getRecipientName())
                        .recipientPhone(request.getRecipientPhone())
                        .postalCode(request.getPostalCode())
                        .addressLine1(request.getAddressLine1())
                        .addressLine2(request.getAddressLine2())
                        .build())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .totalPrice(request.getTotalPrice())
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

            cartService.clearCart(member.getId());
        }

        return orderRepository.save(order).getId();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

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
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByMember(Long memberId) {
        return orderRepository.findByMember_Id(memberId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private OrderResponseDto mapToDto(Order order) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .memberName(order.getMember().getNickName())
                .totalPrice(order.getTotalPrice())
                .itemNames(order.getOrderDetails().stream()
                        .map(detail -> detail.getItem().getItemName())
                        .collect(Collectors.toList()))
                .orderDate(order.getCreatedAt())
                .build();
    }
}
