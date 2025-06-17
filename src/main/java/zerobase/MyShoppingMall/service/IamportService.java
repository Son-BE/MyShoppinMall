package zerobase.MyShoppingMall.service;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
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
        String token = getAccessToken();

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
    }

    private String getAccessToken() {
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
    }

    public JsonNode cancelPayment(String impUid, String reason, Integer amount) {
        String token = getAccessToken();

        Map<String, Object> body = new HashMap<>();
        body.put("imp_uid", impUid);
        body.put("reason", reason != null ? reason : "취소 요청");
        if (amount != null) {
            body.put("amount", amount);
        }

        return webClient.post()
                .uri("https://api.iamport.kr/payments/cancel")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).map(RuntimeException::new))
                .bodyToMono(JsonNode.class)
                .block();
    }


//    private static final String API_URL = "https://api.iamport.kr";
// restTemplate 사용
//    public String getAccessToken() {
//
//        RestTemplate restTemplate = new RestTemplate();
//        Map<String, String> params = new HashMap<>();
//
//        System.out.println("--- 아임포트 토큰 발급 요청 ---");
//        System.out.println("API Key (iamport.api.key): " + impKey);
//        System.out.println("API Secret (iamport.api.secret): " + impSecret);
//        System.out.println("-----------------------------");
//
//        params.put("imp_key", impKey);
//        params.put("imp_secret", impSecret);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL + "/users/getToken", request, Map.class);
//            System.out.println("응답 status code: " + response.getStatusCode());
//            System.out.println("응답 body: " + response.getBody());
//
//            Map body = response.getBody();
//            if (body == null) {
//                throw new RuntimeException("응답 바디가 없습니다");
//            }
//            if (((Number) body.get("code")).intValue() != 0) {
//                throw new RuntimeException("아임포트 API 오류: " + body.get("message"));
//            }
//
//            Map responseData = (Map) body.get("response");
//            return (String) responseData.get("access_token");
//
//        } catch (Exception e) {
//            System.err.println("아임포트 토큰 요청 실패: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("아임포트 토큰 요청 중 오류 발생", e);
//        }
//    }


}

