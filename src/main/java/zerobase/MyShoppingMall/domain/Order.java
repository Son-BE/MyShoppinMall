package zerobase.MyShoppingMall.domain;

import jakarta.persistence.*;
import lombok.*;
import zerobase.MyShoppingMall.type.OrderStatus;
import zerobase.MyShoppingMall.type.PaymentMethod;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Embedded
    private OrderAddress orderAddress;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private int totalPrice;

    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void addOrderDetail(OrderDetail detail) {
        detail.setOrder(this);
        orderDetails.add(detail);
    }

}
