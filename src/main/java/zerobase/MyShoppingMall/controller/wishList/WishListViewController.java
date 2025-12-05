package zerobase.MyShoppingMall.controller.wishList;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import zerobase.MyShoppingMall.dto.wishlist.WishListDto;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.wishList.WishListService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WishListViewController {
    private final WishListService wishListService;
//    private final RecommendationService recommendationService;

    @GetMapping("user/wishList")
    public String viewWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Long memberId = userDetails.getMember().getId();
        List<WishListDto> wishList = wishListService.getWishListByMember(memberId);
        model.addAttribute("wishList", wishList);

//        addWishListPageRecommendations(model, memberId, wishList);

        return "user/wishList";
    }

//    private void addWishListPageRecommendations(Model model, Long memberId, List<WishListDto> wishList) {
//        try {
//            // 1. 위시리스트 상품과 유사한 상품 추천
//            if (!wishList.isEmpty()) {
//                List<Integer> wishlistItemIds = wishList.stream()
//                        .map(item -> item.getItemId().intValue())
//                        .collect(Collectors.toList());
//
//                Map<String, Object> similarItems = recommendationService.getSimilarItems(
//                        wishlistItemIds, 8
//                );
//                model.addAttribute("wishlistSimilarItems", similarItems.get("recommendations"));
//
//                log.debug("위시리스트 유사 상품 추천: {}개",
//                        ((List<?>) similarItems.get("recommendations")).size());
//            }
//
//            // 2. 개인화 추천 - 위시리스트 취향 기반
//            RecommendationResponse personalRecs = recommendationService.getRecommendations(
//                    memberId, 10, "content_based"
//            );
//            model.addAttribute("personalizedWishRecommendations", personalRecs.getRecommendations());
//
//            // 3. 트렌드 상품 - 다른 사용자들이 많이 찜한 상품
//            Map<String, Object> trendingWishItems = recommendationService.getPopularItems(6);
//            model.addAttribute("trendingWishItems", trendingWishItems.get("recommendations"));
//
//            // 4. 실시간 추천 - 최근 활동 기반
//            Map<String, Object> realtimeRecs = recommendationService.getRealTimeRecommendations(
//                    memberId, null, null, 4
//            );
//            model.addAttribute("realtimeWishRecommendations", realtimeRecs.get("recommendations"));
//
//            log.debug("위시리스트 페이지 추천 추가 완료 - 사용자: {}, 찜목록: {}개",
//                    memberId, wishList.size());
//
//        } catch (Exception e) {
//            log.warn("위시리스트 페이지 추천 추가 실패 - 사용자: {}", memberId, e);
//        }
//    }

}
