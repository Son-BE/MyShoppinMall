package zerobase.MyShoppingMall.global;

import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class AIServerErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() ||
                response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(@Nullable URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        log.error("AI 서버 요청 실패 - URL: {}, Method: {}, Status: {} {}",
                url, method, response.getStatusCode(), response.getStatusText());

        if (response.getStatusCode().is5xxServerError()) {
            throw new RuntimeException("AI 서버 내부 오류");
        } else if (response.getStatusCode().is4xxClientError()) {
            throw new RuntimeException("AI 서버 요청 오류");
        }
    }
}
