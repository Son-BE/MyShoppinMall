package zerobase.MyShoppingMall.service.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.repository.item.ItemRepository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final ItemRepository itemRepository;

    private static final String ITEM_CACHE_PREFIX = "item:";
    private static final int CACHE_EXPIRE_MINUTES = 30;

    /**
     * 아이템을 캐시에 저장
     */
    public void cacheItem(ItemResponseDto itemDto) {
        String key = ITEM_CACHE_PREFIX + itemDto.getId();
        redisTemplate.opsForValue().set(key, itemDto, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("아이템 캐시에 저장 완료, key: {}", key);
    }

    /**
     * 캐시에서 아이템 조회
     */
    public ItemResponseDto getCachedItem(Long itemId) {
        String key = ITEM_CACHE_PREFIX + itemId;
        Object cached = redisTemplate.opsForValue().get(key);

        try {
            if (cached != null) {
                if (cached instanceof ItemResponseDto) {
                    log.info("캐시에서 아이템 조회 성공, itemId: {}", itemId);
                    return (ItemResponseDto) cached;
                } else {
                    ItemResponseDto dto = objectMapper.convertValue(cached, ItemResponseDto.class);
                    log.info("캐시에서 아이템 조회 성공 (ObjectMapper 변환), itemId: {}", itemId);
                    return dto;
                }
            }

            log.info("캐시에 아이템 없음, DB에서 조회 itemId: {}", itemId);
            return null;
        } catch (Exception e) {
            log.error("캐시에서 아이템 조회 실패 - itemId: {}, 예외: {}", itemId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 캐시에서 아이템 제거
     */
    public void evictCache(Long itemId) {
        String key = ITEM_CACHE_PREFIX + itemId;
        redisTemplate.delete(key);
        log.info("아이템 캐시 삭제 완료, key: {}", key);
    }

    /**
     * 캐시 우선 조회 - 캐시에 없으면 DB에서 조회 후 캐싱
     */
    public ItemResponseDto getItemWithCache(Long itemId) {
        try {
            // 캐시에서 먼저 조회
            ItemResponseDto cachedItem = getCachedItem(itemId);
            if (cachedItem != null) {
                return cachedItem;
            }

            // 캐시에 없으면 DB에서 조회
            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다. id=" + itemId));

            if (item.getDeleteType() == 'Y') {
                throw new RuntimeException("삭제된 상품입니다.");
            }

            ItemResponseDto itemDto = ItemResponseDto.fromEntity(item);

            // 조회한 데이터를 캐시에 저장
            cacheItem(itemDto);

            return itemDto;

        } catch (Exception e) {
            log.error("getItemWithCache() → itemId: {} 조회 중 예외 발생: {}", itemId, e.getMessage(), e);
            throw e;
        }
    }


}
