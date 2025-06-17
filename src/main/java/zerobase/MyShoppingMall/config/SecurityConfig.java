package zerobase.MyShoppingMall.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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


//jwt 시도
//   @Bean
//   public SecurityFilterChain securityFilterChain(
//           HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
//        http
//                .csrf(crsf -> crsf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        //JWT 사용자용 API(토큰 인증)
//                        .requestMatchers("/api/user/**").authenticated()
//                        //관리자용 API(세션 인증)
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**").permitAll()
//                        .anyRequest().denyAll()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .successHandler(customLoginSuccessHandler())
//                        .permitAll()
//                )
//                .logout(logout -> logout.logoutSuccessUrl("/login")
//                        .invalidateHttpSession(true)
//                        .permitAll()
//                );
//       http.addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);
//
//       return http.build();
//
//   }



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
