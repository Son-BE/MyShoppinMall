package zerobase.MyShoppingMall.dto.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    private String postalCode;
    private String addressLine1;
    private String addressLine2;

    public AddressDto(String postalCode, String addressLine1, String addressLine2) {
        this.postalCode = postalCode;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
    }
}
