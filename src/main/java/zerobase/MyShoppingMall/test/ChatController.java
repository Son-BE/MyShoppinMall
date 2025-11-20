package zerobase.MyShoppingMall.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class ChatController {

    @Qualifier("openaiRestTemplate")
    @Autowired
    private RestTemplate restTemplate;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.api.url}")
    private String apiUrl;

    private static final String SHOPPING_ASSISTANT_PROMPT = """
            온라인 쇼핑몰 AI 어시스턴트입니다.
            궁금하신 점이 있다면 질문해주세요.
            상품 추천, 사용법, 비교 등 쇼핑과 관련된 질문에 답변할 수 있습니다.
            """;

    @GetMapping("/chat")
    public ResponseEntity<?> chat(@RequestParam String prompt) {
        try{
            log.info("Chat request receive: {}", prompt);

            ChatRequest chatRequest = new ChatRequest(model, prompt);
            ChatResponse chatResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);

            if(chatResponse == null || chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Response");
            }

            String response = chatResponse.getChoices().get(0).getMessage().getContent();
            log.info("Chat Response: {}", response);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in chat: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // 쇼핑 어시스턴트 모드
    @PostMapping("/shopping-assistant")
    public ResponseEntity<?> shoppingAssistant(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            log.info("Shopping assistant request: {}", userMessage);

            // 시스템 프롬프트와 함께 요청
            ChatRequest chatRequest = new ChatRequest(model, SHOPPING_ASSISTANT_PROMPT, userMessage);
            chatRequest.setTemperature(0.7);
            chatRequest.setMax_tokens(500);

            ChatResponse chatResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);

            if(chatResponse == null || chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Response");
            }

            String response = chatResponse.getChoices().get(0).getMessage().getContent();

            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            result.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in shopping assistant: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // 상품 추천 테스트
    @PostMapping("/recommend")
    public ResponseEntity<?> recommendProducts(@RequestBody Map<String, String> request) {
        try {
            String category = request.getOrDefault("category", "");
            String budget = request.getOrDefault("budget", "");
            String preference = request.getOrDefault("preference", "");

            String prompt = String.format("""
                다음 조건에 맞는 상품 3개를 추천해주세요:
                - 카테고리: %s
                - 예산: %s
                - 선호사항: %s
                
                각 상품에 대해 이름, 예상 가격, 특징을 간단히 설명해주세요.
                """, category, budget, preference);

            ChatRequest chatRequest = new ChatRequest(model, SHOPPING_ASSISTANT_PROMPT, prompt);
            ChatResponse chatResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);

            if(chatResponse == null || chatResponse.getChoices() == null || chatResponse.getChoices().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Response");
            }

            String response = chatResponse.getChoices().get(0).getMessage().getContent();

            Map<String, Object> result = new HashMap<>();
            result.put("recommendations", response);
            result.put("criteria", Map.of(
                    "category", category,
                    "budget", budget,
                    "preference", preference
            ));

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error in product recommendation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // API 연결 테스트
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        try {
            ChatRequest chatRequest = new ChatRequest(model, "Say 'API is working!' in Korean");
            ChatResponse chatResponse = restTemplate.postForObject(apiUrl, chatRequest, ChatResponse.class);

            if(chatResponse != null && chatResponse.getChoices() != null && !chatResponse.getChoices().isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "status", "connected",
                        "model", model,
                        "message", chatResponse.getChoices().get(0).getMessage().getContent()
                ));
            }

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "error", "message", "No response from OpenAI"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
