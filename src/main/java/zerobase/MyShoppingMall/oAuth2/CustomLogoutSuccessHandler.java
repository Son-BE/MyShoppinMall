package zerobase.MyShoppingMall.oAuth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void onLogoutSuccess(HttpServletRequest request,
                                HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        // 1. 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);

        // 2. 블랙리스트 등록
        if (token != null) {
            try {
                long remainingTime = jwtTokenProvider.getRemainingTime(token);
                jwtTokenProvider.blacklistToken(token);
                log.info("토큰 블랙리스트 등록 완료");
            } catch (Exception e) {
                log.warn("유효하지 않거나 만료된 토큰 - 블랙리스트 등록 불가: {}", e.getMessage());
            }
        }

        // 3. 로그
        if (authentication != null && authentication.getName() != null) {
            log.info("사용자 로그아웃: {}", authentication.getName());
        } else {
            log.info("인증정보 없이 로그아웃");
        }

        // 4. 쿠키 제거
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);

        // 5. 리다이렉트
        response.sendRedirect("/login?logout");
    }
}
