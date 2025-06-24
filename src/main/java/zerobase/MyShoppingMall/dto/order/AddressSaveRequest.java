package zerobase.MyShoppingMall.dto.order;

import lombok.Data;

@Data
public class AddressSaveRequest {
    private String receiverName;
    private String addr;
    private String addrDetail;
    private String postalCode;
    private String receiverPhone;
}