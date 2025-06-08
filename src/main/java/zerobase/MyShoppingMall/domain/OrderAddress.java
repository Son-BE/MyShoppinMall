package zerobase.MyShoppingMall.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress {

    private String recipientName;    // 수령인
    private String postalCode;       //
    private String addressLine1;     // 주소
    private String addressLine2;     // 상세 주소
    private String recipientPhone;      // 전화번호

    public static OrderAddress from(Address address) {
        return OrderAddress.builder()
                .recipientName(address.getReceiverName())
                .recipientPhone(address.getReceiverPhone())
                .postalCode(address.getPostalCode())
                .addressLine1(address.getAddr())
                .addressLine2(address.getAddrDetail())
                .build();
    }

}
