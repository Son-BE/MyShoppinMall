package zerobase.MyShoppingMall.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequest {
    private String message;
    private String sessionId;
}
