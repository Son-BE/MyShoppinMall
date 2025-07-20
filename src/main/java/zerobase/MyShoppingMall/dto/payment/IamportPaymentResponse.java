package zerobase.MyShoppingMall.dto.payment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class IamportPaymentResponse {
    private int code;
    private String message;
    private PaymentData response;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class PaymentData {
        private String imp_uid;
        private String merchant_uid;
        private int amount;
        private String status;
        private String pay_method;
        private String buyer_name;
        private String buyer_email;
        private String buyer_tel;
        private String buyer_addr;
        private String buyer_postcode;
        private String name;
        private String paid_at;
        private String pg_provider;
        private String pg_tid;


    }
}
