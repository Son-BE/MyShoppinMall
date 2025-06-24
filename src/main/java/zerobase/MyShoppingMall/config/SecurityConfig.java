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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 공용 접근 허용

                        .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/items/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/payment/**").permitAll()
                        .requestMatchers("/", "/login", "/signup", "/logout", "register-form", "/css/**", "/js/**", "/create-item").permitAll()

                        //게시판 관련 권한 설정
                        .requestMatchers(HttpMethod.GET, "/board", "/board/", "/board/{id:[\\d]+}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/board/write", "/board/edit/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/board/write", "/board/edit/**", "/board/delete/**").authenticated()

                        //관리자만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        //로그인 시 접근 가능
                        .requestMatchers("/user/**").authenticated()
                        .requestMatchers("/members/**").authenticated()
                        .requestMatchers("/order/**").authenticated()
                        .requestMatchers("/board/write", "/board/edit/**", "/board/delete/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customLoginSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .permitAll()
                );
        return http.build();
    }

//    // --- 1. REST API용 JWT 인증 필터 체인 ---
//    @Bean
//    @Order(1)
//    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/api/**") // /api/** 경로만 적용
//                .csrf(csrf -> csrf.disable()) // API라면 보통 CSRF 비활성화
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(HttpMethod.POST, "/api/members/**").permitAll() // 회원가입 등 공개 API
//                        .requestMatchers("/api/public/**").permitAll() // 공개 API 경로
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(jwtAuthenticationFilter,
//                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
//                .sessionManagement(session -> session.sessionCreationPolicy(
//                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
//        ;
//        return http.build();
//    }
//
//    // --- 2. 웹 폼 로그인용 필터 체인 ---
//    @Bean
//    @Order(2)
//    public SecurityFilterChain formLoginSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/**") // 나머지 모든 경로
//                .authorizeHttpRequests(auth -> auth
//                        // 공용 접근 허용
//                        .requestMatchers(HttpMethod.GET, "/api/items/**").permitAll()
//                        .requestMatchers(HttpMethod.DELETE, "/api/items/**").permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/api/items/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/payment/**").permitAll()
//                        .requestMatchers("/", "/login", "/signup", "/logout", "/register-form", "/css/**", "/js/**", "/create-item").permitAll()
//
//                        // 게시판 관련 권한 설정
//                        .requestMatchers(HttpMethod.GET, "/board", "/board/", "/board/{id:[\\d]+}").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/board/write", "/board/edit/**").authenticated()
//                        .requestMatchers(HttpMethod.POST, "/board/write", "/board/edit/**", "/board/delete/**").authenticated()
//
//                        // 관리자만 접근 가능
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        // 로그인 시 접근 가능
//                        .requestMatchers("/user/**", "/order/**").authenticated()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .successHandler(customLoginSuccessHandler())
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/login")
//                        .invalidateHttpSession(true)
//                        .permitAll()
//                )
//        ;
//        return http.build();
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler customLoginSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                for (GrantedAuthority authority : authorities) {
                    if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                        response.sendRedirect("/dashboard");
                        return;
                    }
                }
                log.info("로그인 성공");
                response.sendRedirect("/items");
            }
        };
    }
}
