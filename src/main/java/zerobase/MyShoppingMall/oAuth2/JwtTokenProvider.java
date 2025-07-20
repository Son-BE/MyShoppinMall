package zerobase.MyShoppingMall.oAuth2;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.redis.RedisTokenService;

import java.security.Key;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private final RedisTokenService redisTokenService;
    private final Key secretKey;
    private final long validityInMilliseconds = 15 * 60 * 1000;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            RedisTokenService redisTokenService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.redisTokenService = redisTokenService;
    }

    public String createAccessToken(String username) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        long remainingMillis = validity.getTime() - now.getTime();
        long remainingSeconds = remainingMillis / 1000;

        log.info(" Access Token 생성 - 사용자 ID: {}", username);
        log.info(" 발급 시각: {}", now);
        log.info(" 유효 만료 시각: {}", validity);
        log.info(" 유효 시간: {}초 ({}분)", remainingSeconds, remainingSeconds);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000L);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
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

    public boolean validateToken(String token) {
        try {
            if (redisTokenService.isBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰입니다.");
                return false;
            }

            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public void blacklistToken(String token) {
        long expiration = getRemainingTime(token);
        redisTokenService.blacklistToken(token, expiration);
    }

    public long getRemainingTime(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey)
                .build().parseClaimsJws(token).getBody().getSubject();
    }




}
