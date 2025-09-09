package zerobase.MyShoppingMall.temps;

import org.springframework.web.client.HttpStatusCodeException;

public class RecommendationException extends RuntimeException {
    public RecommendationException(String message) {
        super(message);
    }

    public RecommendationException(String message, Throwable cause) {
        super(message, cause);
    }
}
