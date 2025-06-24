package zerobase.MyShoppingMall.entity;

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

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Column(unique = true)
    private String merchantUid; // 아임포트 사용 고유번호

    @Column(unique = true)
    private String impUid;// 아임포트 결제 고유번호

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    private int totalPrice;
    private Long usedPoint;

    private LocalDateTime createdAt;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void addOrderDetail(OrderDetail detail) {
        detail.setOrder(this);
        orderDetails.add(detail);
    }

    public void setImpUid(String impUid) {
        this.impUid = impUid;
    }

    public String getImpUid() {
        return this.impUid;
    }


}
