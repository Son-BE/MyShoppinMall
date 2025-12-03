package zerobase.MyShoppingMall.temps.nlp.hugginface;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HuggingFaceService {

    @Value("${huggingface.api.base-url}")
    private String baseUrl;

    @Value("${huggingface.api.token:}")
    private String apiToken;

    @Value("${huggingface.api.timeout:30s}")
    private String timeout;

    @Value("${huggingface.api.max-retries:3}")
    private int maxRetries;

    @Value("${huggingface.models.embedding}")
    private String embeddingModel;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (apiToken != null && !apiToken.isEmpty()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken);
        }
        this.webClient = builder.build();
        log.info("HuggingFace 서비스 초기화 완료 - 모델:{}", embeddingModel);
    }

    @Cacheable(value = "embeddings", key = "#text.hashCode()")
    public double[] getEmbedding(String text) {
        log.debug("임베딩 생성 요청:{}", text.substring(0, Math.min(50, text.length())));

        try {
            String promptedText = addKoreanPrompt(text);
            Map<String, Object> requestBody = Map.of(
                    "inputs", promptedText,
                    "options", Map.of(
                            "wait_for_model", true,
                            "use_cache", true
                    )
            );

            double[] embedding = webClient.post()
                    .uri("/pipeline/feature-extraction/" + embeddingModel)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(double[].class)
                    .retryWhen(Retry.backoff(maxRetries, Duration.ofSeconds(1))
                            .filter(this::isRetryableException))
                    .timeout(Duration.parse("PT" + timeout))
                    .doOnSuccess(result -> log.debug("임베딩 생성 완료: 차원 {}", result.length))
                    .doOnError(error -> log.error("임베딩 생성 실패: {}", error.getMessage()))
                    .block();

            return embedding;

        } catch (Exception e) {
            log.error("HuggingFace API 호출 실패: {}", e.getMessage(), e);
            throw new RuntimeException("임베딩 생성 실패: " + e.getMessage(), e);
        }
    }

    private String addKoreanPrompt(String text) {
        if (embeddingModel.contains("e5")) {
            return "query: " + text;
        }
        return "한국어 패션 상품 검색: " + text;
    }

    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            int statusCode = ex.getStatusCode().value();
            return statusCode == 503 || statusCode == 429 || statusCode == 502 || statusCode == 500;
        }

        return throwable instanceof java.net.ConnectException ||
                throwable instanceof java.util.concurrent.TimeoutException;
    }
}
