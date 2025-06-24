package zerobase.MyShoppingMall.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String addr;

    @Column(name = "addr_detail", nullable = false)
    private String addrDetail;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault; // 기본 배송지

    public void setDefaultAddress(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isDefaultAddress() {
        return this.isDefault;
    }

    public String getAddressLine1() {
        return this.addr;
    }

    public String getAddressLine2() {
        return this.addrDetail;
    }
}
