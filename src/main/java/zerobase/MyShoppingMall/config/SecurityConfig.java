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
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
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
                        .requestMatchers(HttpMethod.GET, "/api/members/check-nickname").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/join", "/api/members/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/check-email").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recommendations/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/recommendations/**").permitAll()

                        .requestMatchers("/api/members/**").authenticated()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/wishList/**").authenticated()
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**", "/oauth2/**","/payment/**", "/admin/upload-and-classify", "/admin/upload/**")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup", "/register-form", "/oauth2/**").permitAll()
                        .requestMatchers("/guest/**").anonymous()
                        .requestMatchers(HttpMethod.POST, "/items/*/reviews").authenticated()
                        .requestMatchers("/items/**").permitAll()
                        .requestMatchers("/board/").permitAll()
                        .requestMatchers(STATIC_RESOURCES).permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/order/**", "/user/cart/**", "/user/wishList").authenticated()
                        .requestMatchers(HttpMethod.GET, "/board/write").authenticated()
                        .requestMatchers(HttpMethod.POST, "/board/write").authenticated()
                        .requestMatchers("/members/**").hasRole("USER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
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
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.debug("인증 필요: {}", request.getRequestURI());
                            response.sendRedirect("/login");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("접근 거부: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
                            response.sendRedirect("/login");
                        })
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
