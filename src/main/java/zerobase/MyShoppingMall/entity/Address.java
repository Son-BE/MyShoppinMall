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
    @JoinColumn(name = "member_id")
    private Member member;

    private String receiverName;
    private String addr;
    private String addrDetail;
    private String postalCode;
    private String receiverPhone;

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
