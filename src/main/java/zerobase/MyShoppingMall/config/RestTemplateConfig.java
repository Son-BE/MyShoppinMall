package zerobase.MyShoppingMall.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import zerobase.MyShoppingMall.global.AIServerErrorHandler;

@Configuration
public class RestTemplateConfig {

    @Value("${ai.server.timeout:90000}")
    private int timeout;

    @Bean
    @Qualifier("aiRestTemplate")
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setErrorHandler(new AIServerErrorHandler());

        return restTemplate;
    }
}
