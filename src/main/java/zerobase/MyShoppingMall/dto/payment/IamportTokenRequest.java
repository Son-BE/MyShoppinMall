package zerobase.MyShoppingMall.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IamportTokenRequest {
    private String imp_key;
    private String imp_secret;
}
