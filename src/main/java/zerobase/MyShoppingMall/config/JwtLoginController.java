package zerobase.MyShoppingMall.config;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class JwtLoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username,
                            loginRequest.password
                    )
            );

            String token = jwtTokenProvider.createToken(authentication.getName());

            return ResponseEntity.ok(Map.of(
                    "username", authentication.getName(),
                    "token", token
            ));
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @PostMapping("/api/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 리프레시 토큰"));
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        String newAccessToken = jwtTokenProvider.createToken(username);

        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }

}
