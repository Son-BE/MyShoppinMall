package zerobase.MyShoppingMall.dto.order;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {
    private Long memberId;
    private Long addressId;
    private String paymentMethod;
    private List<OrderDetailRequest> orderDetails;
}
