//package zerobase.MyShoppingMall.oAuth2;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@Slf4j
//public class JwtAccessDeniedHandler implements AccessDeniedHandler {
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @Override
//    public void handle(HttpServletRequest request,
//                       HttpServletResponse response,
//                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
//
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication != null ? authentication.getName() : "알 수 없는 사용자";
//
//        log.warn("접근 권한 부족 - 사용자: {}, URI: {}, IP: {}, 필요 권한: {}",
//                username,
//                request.getRequestURI(),
//                getClientIP(request),
//                extractRequiredRole(request.getRequestURI()));
//
//        if (isApiRequest(request)) {
//            handleApiRequest(response, accessDeniedException, username);
//        } else {
//            handleWebRequest(request, response, username);
//        }
//    }
//
//    private void handleApiRequest(HttpServletResponse response,
//                                  AccessDeniedException accessDeniedException,
//                                  String username) throws IOException {
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        response.setContentType("application/json;charset=UTF-8");
//
//        Map<String, Object> errorResponse = new HashMap<>();
//        errorResponse.put("error", "FORBIDDEN");
//        errorResponse.put("message", "접근 권한이 없습니다.");
//        errorResponse.put("timestamp", Instant.now().toString());
//        errorResponse.put("status", HttpServletResponse.SC_FORBIDDEN);
//        errorResponse.put("user", username);
//
//        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
//        response.getWriter().write(jsonResponse);
//    }
//
//    private void handleWebRequest(HttpServletRequest request,
//                                  HttpServletResponse response,
//                                  String username) throws IOException {
//        String requestURI = request.getRequestURI();
//
//        // 관리자 페이지 접근 시도인 경우
//        if (requestURI.startsWith("/admin/")) {
//            response.sendRedirect("/access-denied?type=admin");
//        }
//        // 일반 사용자 페이지 접근 시도인 경우
//        else if (requestURI.startsWith("/user/") || requestURI.startsWith("/members/")) {
//            response.sendRedirect("/access-denied?type=user");
//        }
//        // 기타 경우
//        else {
//            response.sendRedirect("/access-denied");
//        }
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
//
//    private String extractRequiredRole(String requestURI) {
//        if (requestURI.startsWith("/api/admin/") || requestURI.startsWith("/admin/")) {
//            return "ADMIN";
//        } else if (requestURI.startsWith("/api/user/") ||
//                requestURI.startsWith("/user/") ||
//                requestURI.startsWith("/members/") ||
//                requestURI.startsWith("/api/members/") ||
//                requestURI.startsWith("/api/cart/") ||
//                requestURI.startsWith("/api/wishList/")) {
//            return "USER";
//        }
//        return "AUTHENTICATED";
//    }
//}
