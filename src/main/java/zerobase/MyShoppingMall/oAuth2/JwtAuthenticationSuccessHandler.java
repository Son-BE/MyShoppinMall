package zerobase.MyShoppingMall.oAuth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.redis.RedisTokenService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService redisTokenService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        Object principal = authentication.getPrincipal();
        String email;

        System.out.println("Principal class: " + principal.getClass().getName());

        if (principal instanceof CustomUserDetails) {
            email = ((CustomUserDetails) principal).getEmail();
        } else if (principal instanceof DefaultOidcUser) {
            DefaultOidcUser oidcUser = (DefaultOidcUser) principal;
            email = oidcUser.getEmail();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            email = oAuth2User.getAttribute("email");
        } else {
            throw new IllegalStateException("Unknown principal type: " + principal.getClass());
        }

        // Access Token 생성
        String accessToken = jwtTokenProvider.createAccessToken(email);

        // Refresh Token 생성 및 Redis에 저장
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        long refreshTokenExpirationMillis = 7 * 24 * 60 * 60 * 1000L; // 7일
        redisTokenService.saveRefreshToken(email, refreshToken, refreshTokenExpirationMillis);

        // Access Token을 쿠키에 저장
        Cookie jwtCookie = new Cookie("access_token", accessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(1800);
        response.addCookie(jwtCookie);

//        response.setHeader("Authorization", "Bearer " + token);

        response.sendRedirect("/");
    }
}
