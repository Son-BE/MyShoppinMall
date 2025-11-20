package zerobase.MyShoppingMall.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotMessage {
    private Long id;
    private Long userId;
    private String context;
    private int isFromChat;
    private LocalDateTime registeredAt;
}
