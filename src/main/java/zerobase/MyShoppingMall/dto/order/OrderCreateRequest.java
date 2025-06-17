package zerobase.MyShoppingMall.dto.order;

import lombok.*;
import zerobase.MyShoppingMall.domain.Address;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {
    private Long memberId;
    private Long addressId;
    private String recipientName;
    private String recipientPhone;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private String deliveryRequest;
    private String paymentMethod;
    private int totalPrice;
    private List<OrderDetailRequest> orderDetails;

    public Address toAddress() {
        Address address = new Address();
        address.setAddr(this.addressLine1);
        address.setAddrDetail(this.addressLine2);
        return address;
    }
}

