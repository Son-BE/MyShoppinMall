package zerobase.MyShoppingMall.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_PREFIX = "refresh:";


    // 블랙리스트에 토큰 저장
    public void blacklistToken(String token, long expirationMillis) {
        log.info("블랙리스트에 토큰 등록: {}, 만료까지 남은 시간: {} ms", token, expirationMillis);

        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "true",
                expirationMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token)
        );
    }

    // 리프레시 토큰 저장
    public void saveRefreshToken(String email, String refreshToken, long expirationMillis) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + email,
                refreshToken,
                expirationMillis,
                TimeUnit.MILLISECONDS
        );
    }

}
