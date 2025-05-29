package zerobase.MyShoppingMall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.*;
import zerobase.MyShoppingMall.dto.Order.OrderRequestDto;
import zerobase.MyShoppingMall.dto.Order.OrderResponseDto;
import zerobase.MyShoppingMall.repository.AddressRepository;
import zerobase.MyShoppingMall.repository.ItemRepository;
import zerobase.MyShoppingMall.repository.MemberRepository;
import zerobase.MyShoppingMall.repository.OrderRepository;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public Long createOrder(OrderRequestDto request) {
        // 회원 및 주소 조회
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new IllegalArgumentException("address not found"));

        // 주문 상세
        List<OrderDetail> orderDetails = request.getOrderDetails().stream().map(dto -> {
            Item item = itemRepository.findById(dto.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("item not found"));

            return OrderDetail.builder()
                    .item(item)
                    .quantity(dto.getQuantity())
                    .price(dto.getPrice())
                    .build();
        }).collect(Collectors.toList());

        // 주문 생성
        Order order = Order.builder()
                .member(member)
                .address(address)
                .totalPrice(request.getTotalPrice())
                .status(request.getStatus())
                .reason(request.getReason())
                .paymentMethod(request.getPaymentMethod())
                .createdAt(LocalDate.now())
                .orderDetails(orderDetails)
                .build();

        orderDetails.forEach(od -> od.setOrder(order));

        orderRepository.save(order);
        return order.getId();
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));
        return OrderResponseDto.fromEntity(order);
    }

    @Transactional
    public void cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order not found"));

        if(order.getStatus() == OrderStatus.CANCELLED) {
            throw  new IllegalArgumentException("this order has been cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setReason(reason);
    }

}
