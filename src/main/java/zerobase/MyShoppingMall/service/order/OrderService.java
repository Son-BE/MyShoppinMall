package zerobase.MyShoppingMall.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import zerobase.MyShoppingMall.type.OrderStatus;
import zerobase.MyShoppingMall.type.PaymentMethod;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final AddressRepository addressRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public OrderResponseDto createOrder(OrderCreateRequest request) {
        Long memberId = request.getMemberId();
        Long addressId = request.getAddressId();

        log.info("memberId: {}", memberId);
        log.info("addressId: {}", addressId);
        log.info("paymentMethod: {}", request.getPaymentMethod());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member id: " + memberId));
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid address id: " + addressId));

        List<OrderDetailRequest> orderDetailsRequest = request.getOrderDetails();
        if (orderDetailsRequest == null) {
            log.warn("orderDetailsRequest is null");
        } else {
            for (OrderDetailRequest detail : orderDetailsRequest) {
                log.info("OrderDetailRequest - itemId: {}, price: {}, quantity: {}",
                        detail.getItemId(), detail.getPrice(), detail.getQuantity());
            }
        }

        List<OrderDetail> orderDetails = orderDetailsRequest.stream()
                .map(detailReq -> {
                    Item item = itemRepository.findById(detailReq.getItemId())
                            .orElseThrow(() -> new IllegalArgumentException("Invalid item id: " + detailReq.getItemId()));
                    return OrderDetail.builder()
                            .item(item)
                            .price(detailReq.getPrice())
                            .quantity(detailReq.getQuantity())
                            .build();
                }).collect(Collectors.toList());

        int totalPrice = orderDetails.stream()
                .mapToInt(od -> od.getPrice() * od.getQuantity())
                .sum();

        Order order = Order.builder()
                .member(member)
                .address(address)
                .orderAddress(OrderAddress.from(address))
                .totalPrice(totalPrice)
                .status(OrderStatus.ORDERED)
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .build();

        orderDetails.forEach(od -> od.setOrder(order));
        order.setOrderDetails(orderDetails);

        orderRepository.save(order);

        // 회원 장바구니 비우기
        cartRepository.deleteByMemberId(memberId);

        return OrderResponseDto.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));
        return OrderResponseDto.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByMember(Long memberId) {
        return orderRepository.findByMemberId(memberId).stream()
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
