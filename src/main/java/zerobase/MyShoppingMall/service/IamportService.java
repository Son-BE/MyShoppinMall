package zerobase.MyShoppingMall.service;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import zerobase.MyShoppingMall.dto.payment.IamportPaymentResponse;
import zerobase.MyShoppingMall.dto.payment.IamportTokenRequest;
import zerobase.MyShoppingMall.dto.payment.IamportTokenResponse;

import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class IamportService {


    private final WebClient webClient;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    public boolean verifyPayment(String impUid, String merchantUid, int expectedAmount) {
        try {
            String token = getAccessToken();
            if (token == null) {
                throw new IllegalStateException("아임포트 토큰 발급 실패");
            }

            IamportPaymentResponse response = webClient.get()
                    .uri("https://api.iamport.kr/payments/" + impUid)
                    .header("Authorization", token)
                    .retrieve()
                    .bodyToMono(IamportPaymentResponse.class)
                    .block();

            if (response == null || response.getResponse() == null) {
                return false;
            }

            int paidAmount = response.getResponse().getAmount();
            String actualMerchantUid = response.getResponse().getMerchant_uid();

            return paidAmount == expectedAmount &&
                    merchantUid != null &&
                    merchantUid.equals(actualMerchantUid);
        } catch (Exception e) {

            System.err.println("결제 검증 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String getAccessToken() {
        try {
            IamportTokenRequest tokenRequest = new IamportTokenRequest(apiKey, apiSecret);

            IamportTokenResponse tokenResponse = webClient.post()
                    .uri("https://api.iamport.kr/users/getToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(tokenRequest)
                    .retrieve()
                    .bodyToMono(IamportTokenResponse.class)
                    .block();

            return tokenResponse != null && tokenResponse.getResponse() != null
                    ? tokenResponse.getResponse().getAccess_token()
                    : null;
        } catch (Exception e) {
            System.err.println("토큰 발급 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public JsonNode cancelPayment(String impUid, String reason, Integer amount) {
        String token = getAccessToken();

        Map<String, Object> body = new HashMap<>();
        body.put("imp_uid", impUid);
        body.put("reason", reason != null ? reason : "취소 요청");
        if (amount != null) {
            body.put("amount", amount);
        }

        JsonNode response = webClient.post()
                .uri("https://api.iamport.kr/payments/cancel")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).map(RuntimeException::new))
                .bodyToMono(JsonNode.class)
                .block();

        // 응답 출력
        System.out.println("아임포트 결제 취소 응답: " + response.toPrettyString());

        return response;
    }

}

