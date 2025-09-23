package zerobase.MyShoppingMall.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ViewHistoryService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "viewed";
    private static final int MAX_HISTORY_SIZE = 10;

    public void addViewedItem(Long memberId, Long itemId) {
        String key = KEY_PREFIX + memberId;
        redisTemplate.opsForList().remove(key, 0, itemId.toString());
        redisTemplate.opsForList().leftPush(key, itemId.toString());
        redisTemplate.opsForList().trim(key, 0, MAX_HISTORY_SIZE - 1);

        log.info("최근 본 상품 기록 - 사용자 ID: {}, 상품 ID: {}", memberId, itemId);
    }

}


