package zerobase.MyShoppingMall.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.service.AiChatService;
import zerobase.MyShoppingMall.dto.ChatRequest;
import zerobase.MyShoppingMall.dto.ChatResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    private final AiChatService aiChatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest,
                                             HttpSession session) {
        String sessionId = session.getId();
        ChatResponse response = aiChatService.chat(chatRequest.getMessage(), sessionId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot Service is running");
    }

}
