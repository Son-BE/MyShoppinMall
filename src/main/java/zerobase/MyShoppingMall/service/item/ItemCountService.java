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
            // 캐시 무효화 - (조회수 변경)
            itemCacheService.evictCache(itemId);
            log.info("아이템 조회수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 조회수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("조회수 증가 중 오류 발생", e);
        }
    }

    /**
     * 아이템 카트 추가 횟수 증가
     */
    @Transactional
    public void increaseCartCount(Long itemId) {
        try {
            itemRepository.incrementCartCount(itemId);
            // 캐시 무효화 - 카트 추가 횟수가 변경되었으므로
            itemCacheService.evictCache(itemId);
            log.info("아이템 카트 횟수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 카트 횟수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("카트 횟수 증가 중 오류 발생", e);
        }
    }

    /**
     * 아이템 주문 횟수 증가
     */
    @Transactional
    public void increaseOrderCount(Long itemId) {
        try {
            itemRepository.incrementOrderCount(itemId);
            // 캐시 무효화 - 주문 횟수가 변경되었으므로
            itemCacheService.evictCache(itemId);
            log.info("아이템 주문 횟수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 주문 횟수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("주문 횟수 증가 중 오류 발생", e);
        }
    }

    /**
     * 아이템 찜 횟수 증가
     */
    @Transactional
    public void increaseWishCount(Long itemId) {
        try {
            itemRepository.incrementWishCount(itemId);
            // 캐시 무효화 - 찜 횟수가 변경되었으므로
            itemCacheService.evictCache(itemId);
            log.info("아이템 찜 횟수 증가 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 찜 횟수 증가 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("찜 횟수 증가 중 오류 발생", e);
        }
    }

    /**
     * 아이템 찜 횟수 감소
     */
    @Transactional
    public void decreaseWishCount(Long itemId) {
        try {
            itemRepository.decrementWishCount(itemId);
            // 캐시 무효화 - 찜 횟수가 변경되었으므로
            itemCacheService.evictCache(itemId);
            log.info("아이템 찜 횟수 감소 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 찜 횟수 감소 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("찜 횟수 감소 중 오류 발생", e);
        }
    }

    /**
     * 아이템의 모든 카운트 정보 초기화 (관리자용)
     */
    @Transactional
    public void resetAllCounts(Long itemId) {
        try {
            // 직접 쿼리로 모든 카운트를 0으로 초기화
            itemRepository.resetItemCounts(itemId);
            itemCacheService.evictCache(itemId);
            log.info("아이템 모든 카운트 초기화 완료, itemId: {}", itemId);
        } catch (Exception e) {
            log.error("아이템 카운트 초기화 실패, itemId: {}, 예외: {}", itemId, e.getMessage());
            throw new RuntimeException("카운트 초기화 중 오류 발생", e);
        }
    }
}
