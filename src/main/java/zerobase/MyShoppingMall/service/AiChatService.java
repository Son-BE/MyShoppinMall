package zerobase.MyShoppingMall.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import zerobase.MyShoppingMall.dto.ChatRequest;
import zerobase.MyShoppingMall.dto.ChatResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatService {

    private final WebClient.Builder webClientBuilder;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public ChatResponse chat(String message, String sessionId) {
        log.info("AI 서비스 호출: message={}", message);

        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        request.setSessionId(sessionId);

        try {
            ChatResponse response = webClientBuilder.build()
                    .post()
                    .uri(aiServiceUrl + "/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatResponse.class)
                    .block();

            log.info("AI 응답 수신 완료");
            return response;
        } catch (Exception e) {
            log.error("AI 서비스 호출 실패: {}", e.getMessage());

            ChatResponse fallback = new ChatResponse();
            fallback.setAnswer("현재 AI 서비스에 연결할 수 없습니다. 잠시 후 다시 시도 해보세요.");
            return fallback;
        }
    }
}
