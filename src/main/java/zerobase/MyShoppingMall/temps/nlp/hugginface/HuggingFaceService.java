package zerobase.MyShoppingMall.temps.nlp.hugginface;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * 텍스트를 임베딩 벡터로 변환
     */
    @Cacheable(value = "embeddings", key = "#text.hashCode()")
    public double[] getEmbedding(String text) {
        log.debug("임베딩 생성 요청:{}", text.substring(0, Math.min(50, text.length())));

        try {
            // 한국어 특화 프롬프트
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

    /**
     * 한국어 패션 특화 프롬프트
     */
    private String addKoreanPrompt(String text) {
        //e5 모델 -> 특화 프롬프트 사용
        if (embeddingModel.contains("e5")) {
            return "query: " + text;
        }

        // 일반적인 한국어 패션 검색 프롬프트
        return "한국어 패션 상품 검색: " + text;
    }

    /**
     * 재시도 가능한 예외인지 확인
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            int statusCode = ex.getStatusCode().value();
            return statusCode == 503 || statusCode == 429 || statusCode == 502 || statusCode == 500;
        }

        // 네트워크 오류 등도 재시도
        return throwable instanceof java.net.ConnectException ||
                throwable instanceof java.util.concurrent.TimeoutException;
    }







}
