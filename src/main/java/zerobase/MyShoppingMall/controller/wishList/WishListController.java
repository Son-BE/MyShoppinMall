package zerobase.MyShoppingMall.controller.wishList;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.wishlist.WishListDto;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.wishList.WishListService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishList")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    //찜목록 조회
    @GetMapping
    public ResponseEntity<List<WishListDto>> getWishList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        List<WishListDto> wishList = wishListService.getWishListByMember(memberId);
        return ResponseEntity.ok(wishList);
    }

    //찜목록 상품 추가
    @PostMapping("/add/{itemId}")
    public ResponseEntity<String> addWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long itemId) {
        Long memberId = userDetails.getMember().getId();
        wishListService.addToWishList(memberId, itemId);
        return ResponseEntity.ok("찜목록에 상품이 추가되었습니다.");
    }

    // 찜목록 상품 삭제
    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<String> removeItemFromWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable Long itemId) {
        Long memberId = userDetails.getMember().getId();
        wishListService.removeFromWishList(memberId, itemId);
        return ResponseEntity.ok("상품이 찜목록에서 삭제되었습니다.");
    }

    // 찜목록 상품 비우기
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearWishList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        wishListService.clearWishList(memberId);

        return ResponseEntity.ok("찜 목록이 비워졌습니다.");
    }

    //찜 상태 확인
    @GetMapping("/status/{itemId}")
    public ResponseEntity<Map<String, Object>> isWished(
            @PathVariable Long itemId,
            @AuthenticationPrincipal zerobase.MyShoppingMall.service.member.CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        boolean isWished = wishListService.isItemWished(memberId, itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("itemId", itemId);
        response.put("wished", isWished);

        return ResponseEntity.ok(response);
    }

    // 찜 토글 추가
    @PostMapping("/toggle/{itemId}")
    public ResponseEntity<Map<String, Object>> toggleWish(
            @PathVariable Long itemId,
            @AuthenticationPrincipal zerobase.MyShoppingMall.service.member.CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        boolean isWished = wishListService.toggleWish(memberId, itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("itemId", itemId);
        response.put("wished", isWished);

        return ResponseEntity.ok(response);
    }


}
