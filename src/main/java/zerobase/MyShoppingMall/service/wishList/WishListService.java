package zerobase.MyShoppingMall.service.wishList;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.entity.WishList;
import zerobase.MyShoppingMall.dto.wishlist.WishListDto;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.repository.wishList.WishListRepository;
import zerobase.MyShoppingMall.service.item.ItemCountService;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishListService {

    private final WishListRepository wishListRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final ItemCountService itemCountService;

    private static final int MAX_WISHLIST_SIZE = 10;

    /**
     * 찜목록 조회하기
     */
    @Transactional(readOnly = true)
    public List<WishListDto> getWishListByMember(Long memberId) {
        validateMemberId(memberId);

        List<WishList> wishLists = wishListRepository.findAllByMemberId(memberId);

        log.info("찜목록 조회 완료 - memberId: {}, 찜 개수: {}", memberId, wishLists.size());

        return wishLists.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 찜목록에 상품 추가하기
     */
    public void addToWishList(Long memberId, Long itemId) {
        validateMemberId(memberId);
        validateItemId(itemId);

        // 이미 찜한 상품인지 확인
        if (wishListRepository.findByMemberIdAndItemId(memberId, itemId).isPresent()) {
            throw new IllegalArgumentException("이미 찜한 상품입니다");
        }

        // 찜목록 개수 제한 확인
        validateWishListSize(memberId);

        try {
            // 엔티티 조회
            Member member = getMemberEntity(memberId);
            Item item = getItemEntity(itemId);

            // 찜목록에 추가
            WishList wishList = WishList.builder()
                    .member(member)
                    .item(item)
                    .build();

            wishListRepository.save(wishList);

            // 아이템 찜 카운트 증가
            itemCountService.increaseWishCount(itemId);

            log.info("찜목록 추가 완료 - memberId: {}, itemId: {}", memberId, itemId);

        } catch (Exception e) {
            log.error("찜목록 추가 실패 - memberId: {}, itemId: {}, 예외: {}", memberId, itemId, e.getMessage());
            throw new RuntimeException("찜목록 추가 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 찜목록에서 상품 삭제
     */
    public void removeFromWishList(Long memberId, Long itemId) {
        validateMemberId(memberId);
        validateItemId(itemId);

        try {
            // 찜목록에서 삭제
            int deletedCount = wishListRepository.deleteByMemberIdAndItemId(memberId, itemId);

            if (deletedCount > 0) {
                // 아이템 찜 카운트 감소
                itemCountService.decreaseWishCount(itemId);
                log.info("찜목록 삭제 완료 - memberId: {}, itemId: {}", memberId, itemId);
            } else {
                log.warn("삭제할 찜목록이 없음 - memberId: {}, itemId: {}", memberId, itemId);
            }

        } catch (Exception e) {
            log.error("찜목록 삭제 실패 - memberId: {}, itemId: {}, 예외: {}", memberId, itemId, e.getMessage());
            throw new RuntimeException("찜목록 삭제 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 찜목록 전체 비우기
     */
    public void clearWishList(Long memberId) {
        validateMemberId(memberId);

        try {
            // 삭제할 찜목록들의 itemId 조회
            List<WishList> wishLists = wishListRepository.findAllByMemberId(memberId);
            List<Long> itemIds = wishLists.stream()
                    .map(wl -> wl.getItem().getId())
                    .collect(Collectors.toList());

            // 찜목록 전체 삭제
            wishListRepository.deleteAllByMemberId(memberId);

            // 각 아이템의 찜 카운트 감소
            itemIds.forEach(itemCountService::decreaseWishCount);

            log.info("찜목록 전체 삭제 완료 - memberId: {}, 삭제된 아이템 수: {}", memberId, itemIds.size());

        } catch (Exception e) {
            log.error("찜목록 전체 삭제 실패 - memberId: {}, 예외: {}", memberId, e.getMessage());
            throw new RuntimeException("찜목록 전체 삭제 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 찜 상태 확인
     */
    @Transactional(readOnly = true)
    public boolean isItemWished(Long memberId, Long itemId) {
        if (memberId == null || itemId == null) {
            return false;
        }
        return wishListRepository.existsByMemberIdAndItemId(memberId, itemId);
    }

    /**
     * 찜 상태 토글 (찜하기/찜해제)
     */
    public boolean toggleWish(Long memberId, Long itemId) {
        validateMemberId(memberId);
        validateItemId(itemId);

        Optional<WishList> existingWish = wishListRepository.findByMemberIdAndItemId(memberId, itemId);

        if (existingWish.isPresent()) {
            // 찜 해제
            removeFromWishList(memberId, itemId);
            log.info("찜 해제 완료 - memberId: {}, itemId: {}", memberId, itemId);
            return false;
        } else {
            // 찜 추가
            addToWishList(memberId, itemId);
            log.info("찜 추가 완료 - memberId: {}, itemId: {}", memberId, itemId);
            return true;
        }
    }

    /**
     * 사용자의 찜목록 개수 조회
     */
    @Transactional(readOnly = true)
    public int getWishListCount(Long memberId) {
        validateMemberId(memberId);
        return wishListRepository.countByMemberId(memberId);
    }

    /**
     * 아이템의 총 찜 수 조회 (통계용)
     */
    @Transactional(readOnly = true)
    public int getTotalWishCountForItem(Long itemId) {
        validateItemId(itemId);
        return wishListRepository.countByItemId(itemId);
    }

    // === Private Helper Methods ===

    /**
     * WishList 엔티티를 DTO로 변환
     */
    private WishListDto convertToDto(WishList wishList) {
        return WishListDto.builder()
                .id(wishList.getId())
                .memberId(wishList.getMember().getId())
                .itemId(wishList.getItem().getId())
                .itemName(wishList.getItem().getItemName())
                .itemImagePath(wishList.getItem().getImageUrl())
                .itemPrice(wishList.getItem().getPrice())
                .build();
    }

    /**
     * 멤버 ID 유효성 검사
     */
    private void validateMemberId(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
    }

    /**
     * 아이템 ID 유효성 검사
     */
    private void validateItemId(Long itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("아이템 ID는 필수입니다");
        }
    }

    /**
     * 찜목록 크기 제한 검사
     */
    private void validateWishListSize(Long memberId) {
        int currentItemCount = wishListRepository.countByMemberId(memberId);
        if (currentItemCount >= MAX_WISHLIST_SIZE) {
            throw new IllegalArgumentException("찜목록은 최대 " + MAX_WISHLIST_SIZE + "개까지 등록하실 수 있습니다.");
        }
    }

    /**
     * 멤버 엔티티 조회
     */
    private Member getMemberEntity(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));
    }

    /**
     * 아이템 엔티티 조회
     */
    private Item getItemEntity(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + itemId));
    }
}