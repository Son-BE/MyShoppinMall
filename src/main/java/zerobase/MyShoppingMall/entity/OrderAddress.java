package zerobase.MyShoppingMall.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddress {
    private String recipientName;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private String recipientPhone;

}
