    //package zerobase.MyShoppingMall.oAuth2;
    //
    //
    //import jakarta.servlet.http.Cookie;
    //import jakarta.servlet.http.HttpServletRequest;
    //import jakarta.servlet.http.HttpServletResponse;
    //import lombok.RequiredArgsConstructor;
    //import lombok.extern.slf4j.Slf4j;
    //import org.springframework.http.HttpStatus;
    //import org.springframework.http.ResponseEntity;
    //import org.springframework.security.authentication.AuthenticationManager;
    //import org.springframework.security.authentication.BadCredentialsException;
    //import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    //import org.springframework.security.core.Authentication;
    //import org.springframework.security.core.AuthenticationException;
    //import org.springframework.web.bind.annotation.PostMapping;
    //import org.springframework.web.bind.annotation.RequestBody;
    //import org.springframework.web.bind.annotation.RestController;
    //
    //import java.util.Map;
    //
    //@Slf4j
    //@RestController
    //@RequiredArgsConstructor
    //public class JwtLoginController {
    //
    //    private final AuthenticationManager authenticationManager;
    //    private final JwtTokenProvider jwtTokenProvider;
    //
    //    public static class LoginRequest {
    //        public String username;
    //        public String password;
    //    }
    //
    //    @PostMapping("/api/login")
    //    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
    //                                   HttpServletResponse response) {
    //        try {
    //            Authentication authentication = authenticationManager.authenticate(
    //                    new UsernamePasswordAuthenticationToken(
    //                            loginRequest.username,
    //                            loginRequest.password
    //                    )
    //            );
    //
    ////            String token = jwtTokenProvider.createToken(authentication.getName());
    //            String accessToken = jwtTokenProvider.createToken(authentication.getName());
    //            String refreshToken = jwtTokenProvider.createRefreshToken(authentication.getName());
    //
    //            Cookie accessCookie = new Cookie("access_token", accessToken);
    //            accessCookie.setHttpOnly(true);
    //            accessCookie.setPath("/");
    //            accessCookie.setMaxAge(60 * 60); // 1시간
    //
    //            Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
    //            refreshCookie.setHttpOnly(true);
    //            refreshCookie.setPath("/");
    //            refreshCookie.setMaxAge(60 * 60 * 24 * 7); // 7일
    //
    //            response.addCookie(accessCookie);
    //            response.addCookie(refreshCookie);
    //
    //            log.info("로그인 성공: {}", authentication.getName());
    //
    //            return ResponseEntity.ok(Map.of(
    //                    "username", authentication.getName(),
    //                    "accessToken", accessToken
    //            ));
    //        } catch (AuthenticationException e) {
    //            log.warn(" 로그인 실패 - 아이디 또는 비밀번호 오류: {}", loginRequest.username);
    //            throw new BadCredentialsException("Invalid username or password");
    //        }
    //    }
    //
    ////    @PostMapping("/api/refresh-token")
    //////    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
    ////      public ResponseEntity<?> refreshToken(HttpServletRequest request) {
    //////        String refreshToken = request.get("refreshToken");
    ////        String refreshToken = getCookieValue(request, "refresh_token");
    ////
    ////        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
    ////            log.warn("유효하지 않은 리프레시 토큰: {}", refreshToken);
    ////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 리프레시 토큰"));
    ////        }
    ////
    ////        String username = jwtTokenProvider.getUsername(refreshToken);
    ////        String newAccessToken = jwtTokenProvider.createToken(username);
    ////
    ////
    ////        log.info("액세스 토큰 재발급 - 사용자: {}", username);
    ////
    ////        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    ////    }
    //
    ////    @PostMapping("/api/logout")
    ////    public ResponseEntity<?> logout(HttpServletResponse response) {
    ////        // access_token 삭제
    ////        Cookie accessCookie = new Cookie("access_token", null);
    ////        accessCookie.setHttpOnly(true);
    ////        accessCookie.setPath("/");
    ////        accessCookie.setMaxAge(0);
    ////
    ////        // refresh_token 삭제
    ////        Cookie refreshCookie = new Cookie("refresh_token", null);
    ////        refreshCookie.setHttpOnly(true);
    ////        refreshCookie.setPath("/");
    ////        refreshCookie.setMaxAge(0);
    ////
    ////        response.addCookie(accessCookie);
    ////        response.addCookie(refreshCookie);
    ////
    ////        log.info("로그아웃 완료 - 쿠키 삭제됨");
    ////
    ////        return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
    ////    }
    //
    //    private String getCookieValue(HttpServletRequest request, String name) {
    //        if (request.getCookies() == null) return null;
    //        for (Cookie cookie : request.getCookies()) {
    //            if (cookie.getName().equals(name)) {
    //                return cookie.getValue();
    //            }
    //        }
    //        return null;
    //    }
    //
    //}
