//package zerobase.MyShoppingMall.oAuth2;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void commence(HttpServletRequest request,
//                         HttpServletResponse response,
//                         AuthenticationException authException) throws IOException, ServletException {
//
//        log.warn("인증되지 않은 접근 시도 - URI: {}, IP: {}, User-Agent: {}",
//                request.getRequestURI(),
//                getClientIP(request),
//                request.getHeader("User-Agent"));
//
//        String requestURI = request.getRequestURI();
//
//        // API 요청인지 확인
//        if (isApiRequest(request)) {
//            handleApiRequest(response, authException);
//        } else {
//            handleWebRequest(request, response);
//        }
//    }
//
//    private void handleApiRequest(HttpServletResponse response, AuthenticationException authException)
//            throws IOException {
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json;charset=UTF-8");
//
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error", "UNAUTHORIZED");
//        errorResponse.put("message", "인증이 필요합니다.");
//        errorResponse.put("timestamp", Instant.now().toString());
//        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
//
//        // 구체적인 오류 정보 추가 (개발 환경에서만)
//        String detailMessage = authException.getMessage();
//        if (detailMessage != null) {
//            if (detailMessage.contains("expired")) {
//                errorResponse.put("error", "TOKEN_EXPIRED");
//                errorResponse.put("message", "토큰이 만료되었습니다.");
//            } else if (detailMessage.contains("invalid")) {
//                errorResponse.put("error", "INVALID_TOKEN");
//                errorResponse.put("message", "유효하지 않은 토큰입니다.");
//            }
//        }
//
//        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
//        response.getWriter().write(jsonResponse);
//    }
//
//    private void handleWebRequest(HttpServletRequest request, HttpServletResponse response)
//            throws IOException {
//        // 웹 요청의 경우 로그인 페이지로 리다이렉트
//        String loginUrl = "/login";
//
//        // 원래 요청 URL을 파라미터로 저장 (로그인 후 리다이렉트용)
//        String requestURI = request.getRequestURI();
//        String queryString = request.getQueryString();
//
//        if (queryString != null) {
//            requestURI += "?" + queryString;
//        }
//
//        // 로그인 페이지가 아닌 경우에만 리다이렉트 URL 저장
//        if (!requestURI.equals("/login") && !requestURI.startsWith("/login?")) {
//            loginUrl += "?redirect=" + java.net.URLEncoder.encode(requestURI, "UTF-8");
//        }
//
//        response.sendRedirect(loginUrl);
//    }
//
//    private boolean isApiRequest(HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        String acceptHeader = request.getHeader("Accept");
//        String contentType = request.getHeader("Content-Type");
//
//        return requestURI.startsWith("/api/") ||
//                (acceptHeader != null && acceptHeader.contains("application/json")) ||
//                (contentType != null && contentType.contains("application/json"));
//    }
//
//    private String getClientIP(HttpServletRequest request) {
//        String xForwardedFor = request.getHeader("X-Forwarded-For");
//        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
//            return xForwardedFor.split(",")[0].trim();
//        }
//
//        String xRealIP = request.getHeader("X-Real-IP");
//        if (xRealIP != null && !xRealIP.isEmpty()) {
//            return xRealIP;
//        }
//
//        return request.getRemoteAddr();
//    }
//}
