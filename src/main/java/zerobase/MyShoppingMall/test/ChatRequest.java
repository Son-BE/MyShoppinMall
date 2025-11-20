package zerobase.MyShoppingMall.test;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChatRequest {
    private String model;
    private List<Message> messages;
    private int n = 1;
    private double temperature = 0.7;
    private int max_tokens = 500;

    public ChatRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }

    public ChatRequest(String model, String systemMessage, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("system", systemMessage));
        this.messages.add(new Message("user", prompt));
    }
}
