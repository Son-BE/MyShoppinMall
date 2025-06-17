package zerobase.MyShoppingMall.dto.payment;

import lombok.Data;

@Data
public class IamportTokenResponse {
    private int code;
    private String message;
    private TokenResponse response;

    @Data
    public static class TokenResponse {
        private String access_token;
        private long now;
        private long expired_at;
    }
}
