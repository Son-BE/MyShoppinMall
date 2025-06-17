package zerobase.MyShoppingMall;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class IamportService {

    private static final String API_URL = "https://api.iamport.kr";
    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;



    public String getAccessToken() {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL + "/users/getToken", request, Map.class);

        return (String) ((Map<?, ?>) response.getBody().get("response")).
                get("access_token");
    }


}

