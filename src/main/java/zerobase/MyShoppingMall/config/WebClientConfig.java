package zerobase.MyShoppingMall.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @Qualifier("iamportWebClient")
    public WebClient iamportWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.iamport.kr").build();
    }

    //    @Bean
//    @Qualifier("recommendWebClient")
//    public WebClient recommendWebClient(WebClient.Builder builder) {
//        return builder.baseUrl("http://flask-api:5000").build();
//    }
//    @Bean
//    @Qualifier("recommendWebClient")
//    public WebClient recommendWebClient() {
//        return WebClient.builder()
//                .baseUrl("http://flask-api:5000")
//                .build();
//    }
}
