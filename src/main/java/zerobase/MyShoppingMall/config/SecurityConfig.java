package zerobase.MyShoppingMall.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import zerobase.MyShoppingMall.oAuth2.*;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;
    private final CustomOidcUserService oidcUserService;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationSuccessHandler jwtSuccessHandler;
    private static final String[] STATIC_RESOURCES = {
            "/images/**", "/css/**", "/js/**", "/favicon.ico"
    };

    /**
     * /api/** 경로 전용
     * CSRF 비활성화, 세션 사용 안 함 (STATELESS), 폼 로그인/HTTP Basic 비활성화
     * 일부 /api/members/** 및 /api/cart/** 요청은 모두 허용
     * 나머지 /api/** 요청은 JWT 인증 필터를 통해 인증 필요
     * jwtAuthenticationFilter를 Spring Security 필터 체인에 등록
     * 토큰 기반 인증 처리
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/members/join", "/api/members/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/check-email").permitAll()
                        .requestMatchers("/api/members/**").authenticated()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/wishList/**").authenticated()
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * /, /login, /signup, /register-form, /oauth2/**, 정적 리소스 → 모두 허용
     * /board/**, /order/**, /user/cart/**, /user/wishList → 로그인 필요
     * /admin/** → ADMIN 권한 필요
     * /members/** → USER 권한 필요
     * OAuth2 사용자 정보는 CustomOAuth2UserService와 CustomOidcUserService로 처리
     * 로그인 성공 시 JWT 성공 핸들러로 처리
     * 로그아웃 성공 시 사용자 정의 핸들러 사용
     * 세션 무효화
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup", "/register-form", "/oauth2/**").permitAll()
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        .requestMatchers("/board/**", "/order/**", "/user/cart/**", "/user/wishList").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/members/**").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(roleBasedLoginSuccessHandler())
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)
                                .oidcUserService(oidcUserService)
                        )
                        .successHandler(jwtSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .invalidateHttpSession(true)
                        .permitAll()
                );

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedLoginSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

                boolean isAdmin = authorities.stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    log.info("관리자 로그인 성공");
                    response.sendRedirect("/dashboard");
                } else {
                    log.info("일반 사용자 로그인 성공");
                    jwtSuccessHandler.onAuthenticationSuccess(request, response, authentication);
                }
            }
        };
    }

}
