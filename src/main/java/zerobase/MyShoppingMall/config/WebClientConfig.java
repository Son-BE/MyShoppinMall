package zerobase.MyShoppingMall.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openai.api.key:}")
    private String apiKey;

    @Bean
    @Qualifier("iamportWebClient")
    public WebClient iamportWebClient(WebClient.Builder builder) {
        return builder.baseUrl("https://api.iamport.kr").build();
    }

    @Bean
    @Qualifier("nlpWebClient")
    public WebClient nlpWebClient(WebClient.Builder builder,
                                  @Value("${nlp.service.url:http://nlp-server:5001}") String nlpServiceUrl) {
        return builder.baseUrl(nlpServiceUrl).build();
    }

    @Bean
    @Qualifier("chatbot")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }


}
