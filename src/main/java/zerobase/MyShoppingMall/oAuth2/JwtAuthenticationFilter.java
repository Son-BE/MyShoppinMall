package zerobase.MyShoppingMall.oAuth2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private static final List<String> NO_CHECK_URLS = List.of(
            "/favicon.ico", "/css/", "/js/", "/images/", "/webjars/", "/error"
    );
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

//            String token = jwtTokenProvider.resolveToken(request);
        String token = resolveToken(request);
        String path = request.getRequestURI();
        for (String url : NO_CHECK_URLS) {
            if (path.startsWith(url)) {
                chain.doFilter(request, response);
                return;
            }
        }

        if (token != null) {
            log.info("JWT 토큰 감지 : {}", token);
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    log.info("유효한 JWT 토큰입니다");

                    String email = jwtTokenProvider.getUsername(token);
                    Member member = memberRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            new CustomUserDetails(member, Map.of()), null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("만료된 JWT 토큰입니다: {}", e.getMessage());
                SecurityContextHolder.clearContext();

                Cookie expiredCookie = new Cookie("access_token", null);
                expiredCookie.setPath("/");
                expiredCookie.setMaxAge(0); // 즉시 만료
                response.addCookie(expiredCookie);

                response.sendRedirect("/?expired=true");
                return;
            } catch (Exception e) {
                log.warn("JWT 토큰 검증 중 예외 발생: {}", e.getMessage());
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"message\": \"잘못된 인증 정보입니다.\"}");
                return;
            }
        } else {
            log.warn("JWT 토큰이 존재하지 않습니다");
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

}
