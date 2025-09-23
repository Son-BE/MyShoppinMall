package zerobase.MyShoppingMall.service.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.repository.item.ItemRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCountService {

    private final ItemRepository itemRepository;
    private final ItemCacheService itemCacheService;

    /**
     * 아이템 조회수 증가
     */
    @Transactional
    public void increaseViewCount(Long itemId) {
        try {
            itemRepository.incrementViewCount(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 조회수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 조회수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("조회수 증가 중 오류 발생", e);
        }
    }
    @Transactional
    public void increaseCartCount(Long itemId) {
        try {
            itemRepository.incrementCartCount(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 카트 횟수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 카트 횟수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("카트 횟수 증가 중 오류 발생", e);
        }
    }
    @Transactional
    public void increaseOrderCount(Long itemId) {
        try {
            itemRepository.incrementOrderCount(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 주문 횟수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 주문 횟수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("주문 횟수 증가 중 오류 발생", e);
        }
    }
    @Transactional
    public void increaseWishCount(Long itemId) {
        try {
            itemRepository.incrementWishCount(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 찜 횟수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 찜 횟수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("찜 횟수 증가 중 오류 발생", e);
        }
    }
    @Transactional
    public void decreaseWishCount(Long itemId) {
        try {
            itemRepository.decrementWishCount(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 찜 횟수 감소 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 찜 횟수 감소 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("찜 횟수 감소 중 오류 발생", e);
        }
    }
    @Transactional
    public void resetAllCounts(Long itemId) {
        try {
            itemRepository.resetItemCounts(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 모든 카운트 초기화 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 카운트 초기화 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("카운트 초기화 중 오류 발생", e);
        }
    }
}
