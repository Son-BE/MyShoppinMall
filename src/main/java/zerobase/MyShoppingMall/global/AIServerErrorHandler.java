package zerobase.MyShoppingMall.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Slf4j
public class AIServerErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.valueOf(response.getStatusCode().value());
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        HttpStatus statusCode = HttpStatus.valueOf(response.getStatusCode().value());

        log.error("AI 서버 응답 오류 - URL: {}, Method: {}, 상태코드: {}, 메시지: {}",
                url, method, statusCode, response.getStatusText());

        switch (statusCode) {
            case BAD_REQUEST -> throw new RuntimeException("AI 서버: 잘못된 요청 형식입니다.");
            case INTERNAL_SERVER_ERROR -> throw new RuntimeException("AI 서버: 내부 서버 오류가 발생했습니다.");
            case SERVICE_UNAVAILABLE -> throw new RuntimeException("AI 서버: 서비스를 사용할 수 없습니다.");
            default -> throw new RuntimeException("AI 서버: 알 수 없는 오류가 발생했습니다. 상태코드: " + statusCode);
        }
    }

}
