package zerobase.MyShoppingMall.controller.wishList;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.dto.wishlist.WishListDto;

import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.wishList.WishListService;
import zerobase.MyShoppingMall.temps.RecommendationService;

import java.util.List;

@Controller
@RequestMapping("/wishList")
@RequiredArgsConstructor
@Slf4j
public class WishListController {

    private final WishListService wishListService;
    private final RecommendationService recommendationService;

    //찜목록 조회
    @GetMapping
    public ResponseEntity<List<WishListDto>> getWishList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        List<WishListDto> wishList = wishListService.getWishListByMember(memberId);
        return ResponseEntity.ok(wishList);
    }

    //찜목록 상품 추가
    @PostMapping("/add")
    public String addWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam Long itemId, RedirectAttributes redirectAttributes) {
        Member member = userDetails.getMember();
        try{
            wishListService.addToWishList(member.getId(), itemId);

            // 🎯 추천 시스템: 위시리스트 추가 상호작용 기록
            recordUserInteraction(member.getId(), itemId, "like");

            redirectAttributes.addFlashAttribute("message", "찜목록에 상품이 추가되었습니다!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
        }

        return "redirect:/user/wishList";
    }

    // 찜목록 상품 삭제
    @PostMapping("/remove")
    public String removeItemFromWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam Long itemId) {
        Member member = userDetails.getMember();
        wishListService.removeFromWishList(member.getId(), itemId);
        return "redirect:/user/wishList";
    }

    // 찜목록 상품 비우기
    @PostMapping("/clear")
    public String clearWishList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        wishListService.clearWishList(member.getId());
        return "redirect:/user/wishList";
    }

    /**
     * 사용자 상호작용 기록 (안전하게 처리)
     */
    private void recordUserInteraction(Long memberId, Long itemId, String action) {
        try {
            recommendationService.recordUserInteraction(memberId, itemId, action);
            log.debug("위시리스트 상호작용 기록 - 사용자: {}, 상품: {}, 액션: {}", memberId, itemId, action);
        } catch (Exception e) {
            log.warn("위시리스트 상호작용 기록 실패 - 사용자: {}, 상품: {}, 액션: {}",
                    memberId, itemId, action, e);
        }
    }
//    //찜목록 조회
//    @GetMapping
//    public ResponseEntity<List<WishListDto>> getWishList(
//            @AuthenticationPrincipal CustomUserDetails userDetails) {
//        Long memberId = userDetails.getMember().getId();
//        List<WishListDto> wishList = wishListService.getWishListByMember(memberId);
//        return ResponseEntity.ok(wishList);
//    }
//
//    //찜목록 상품 추가
//    @PostMapping("/add")
//    public String addWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
//                              @RequestParam Long itemId, RedirectAttributes redirectAttributes) {
//        Member member = userDetails.getMember();
//        try{
//            wishListService.addToWishList(member.getId(), itemId);
//            redirectAttributes.addFlashAttribute("message", "찜목록에 상품이 추가되었습니다!");
//        } catch (IllegalArgumentException e) {
//            redirectAttributes.addFlashAttribute("message", e.getMessage());
//        }
//
//        return "redirect:/user/wishList";
//    }
//
//    // 찜목록 상품 삭제
//    @PostMapping("/remove")
//    public String removeItemFromWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                         @RequestParam Long itemId) {
//        Member member = userDetails.getMember();
//        wishListService.removeFromWishList(member.getId(), itemId);
//        return "redirect:/user/wishList";
//    }
//
//    // 찜목록 상품 비우기
//    @PostMapping("/clear")
//    public String clearWishList(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        Member member = userDetails.getMember();
//        wishListService.clearWishList(member.getId());
//        return "redirect:/user/wishList";
//    }

}
