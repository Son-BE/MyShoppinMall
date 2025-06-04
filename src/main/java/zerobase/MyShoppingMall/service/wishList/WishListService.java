package zerobase.MyShoppingMall.service.wishList;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.domain.WishList;
import zerobase.MyShoppingMall.dto.wishlist.WishListDto;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.repository.wishList.WishListRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishListService {

    private final WishListRepository wishListRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    //찜목록 조회하기
    @Transactional(readOnly = true)
    public List<WishListDto> getWishListByMember(Long memberId) {
        List<WishList> wishLists = wishListRepository.findAllByMemberId(memberId);
        return wishLists.stream()
                .map(wl -> WishListDto.builder()
                        .id(wl.getId())
                        .memberId(wl.getMember().getId())
                        .itemId(wl.getItem().getId())
                        .itemName(wl.getItem().getItemName())
                        .itemImagePath(
                                wl.getItem().getItemImages().isEmpty()
                                        ? null
                                        : wl.getItem().getItemImages().get(0).getItemPath())
                        .itemPrice(wl.getItem().getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    //찜목록에 상품 추가하기
    public void addToWishList(Long memberId, Long itemId) {
        if(wishListRepository.findByMemberIdAndItemId(memberId, itemId).isPresent()) {
            throw new IllegalArgumentException("이미 찜한 상품입니다");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 ID가 존재하지 않습니다."));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 ID가 존재하지 않습니다"));

        WishList wishList = WishList.builder()
                .member(member)
                .item(item)
                .build();

        wishListRepository.save(wishList);
    }

    //찜목록에서 상품 삭제
    public void removeFromWishList(Long memberId, Long itemId) {
        wishListRepository.deleteByMemberIdAndItemId(memberId, itemId);
    }
    //찜목록 비우기
    public void clearWishList(Long memberId) {
        wishListRepository.deleteAllByMemberId(memberId);
    }

    // 찜 상태 확인
    @Transactional(readOnly = true)
    public boolean isItemWished(Long memberId, Long itemId) {
        return wishListRepository.existsByMemberIdAndItemId(memberId, itemId);
    }

    public boolean toggleWish(Long memberId, Long itemId) {
        Optional<WishList> wish = wishListRepository.findByMemberIdAndItemId(memberId, itemId);

        if (wish.isPresent()) {
            wishListRepository.delete(wish.get());
            return false;
        } else {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("회원 ID가 존재하지 않습니다."));

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new IllegalArgumentException("아이템 ID가 존재하지 않습니다."));

            WishList newWish = WishList.builder()
                    .member(member)
                    .item(item)
                    .build();

            wishListRepository.save(newWish);
            return true;
        }
    }
}


