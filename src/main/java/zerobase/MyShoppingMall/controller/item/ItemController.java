package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.dto.item.neo.PaginationInfo;
import zerobase.MyShoppingMall.entity.Review;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.repository.item.ReviewRepository;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.service.item.PaginationService;
import zerobase.MyShoppingMall.service.item.ReviewServiceImpl;
import zerobase.MyShoppingMall.service.item.ViewHistoryService;
import zerobase.MyShoppingMall.service.order.OrderService;
import zerobase.MyShoppingMall.temps.CategoryStats;
import zerobase.MyShoppingMall.temps.CategoryStatsService;
import zerobase.MyShoppingMall.type.Gender;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final ViewHistoryService viewHistoryService;
    private final ReviewServiceImpl reviewService;
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final PaginationService paginationService;
//    private final RecommendationService recommendationService;
    private final CategoryStatsService categoryStatsService;

    @GetMapping
    public String getItemsListPage(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "category", required = false) String subCategory,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Gender genderEnum = parseGender(gender);
        int validatedSize = paginationService.validateAndCorrectPageSize(16);

        Page<ItemResponseDto> itemPage;

        // 검색어가 있으면 검색, 없으면 필터링
        if (search != null && !search.trim().isEmpty()) {
            Pageable pageable = PageRequest.of(page, validatedSize);
            itemPage = itemService.searchItems(search.trim(), sort, pageable);
            model.addAttribute("searchQuery", search);
        } else {
            itemPage = itemService.findItems(genderEnum, sort, subCategory, page, validatedSize);
        }

        PaginationInfo paginationInfo = paginationService.createPaginationInfo(itemPage);

        addItemListPageAttributes(model, itemPage, paginationInfo, gender, sort, subCategory);
//        addItemListPageRecommendations(model, userDetails);

        log.info("상품 목록 페이지 로드 - 검색: {}, 필터: gender={}, sort={}, category={}, page={}",
                search, gender, sort, subCategory, page);

        return "items/list";
    }

    @GetMapping("/detail/{id}")
    public String getItemDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long memberId = getMemberId(userDetails);

        // 조회 이력 기록
        recordViewHistory(memberId, id);

        // 아이템 정보 조회 (캐시 활용)
        ItemResponseDto item = itemService.getItem(id, memberId);
        itemService.increaseViewCount(id);

        // 리뷰 정보 조회
        List<Review> reviews = reviewRepository.findByItemIdOrderByCreatedAtDesc(id);
        Double averageRating = reviewService.getAverageRating(id);
        boolean canWriteReview = checkCanWriteReview(memberId, id);

        // 주문 정보 조회
        Long orderId = getLatestOrderId(memberId, id);

        // 모델에 기본 정보 추가
        addItemDetailAttributes(model, item, reviews, averageRating, canWriteReview, orderId);

//        // 추천 정보 추가 (실패해도 페이지는 정상 표시)
//        addItemDetailRecommendations(model, memberId, id, item);
//
//        // 사용자 상호작용 기록 (비동기 처리 권장)
//        recordUserInteraction(memberId, id, "view");

        log.info("아이템 상세 페이지 로드 - itemId: {}, memberId: {}", id, memberId);
        return "user/detail";
    }

    // ========== Private Helper Methods ==========

    private Gender parseGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            return null;
        }
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 gender 파라미터: {}", gender);
            return null;
        }
    }

    private Long getMemberId(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getMember().getId() : null;
    }

    private void recordViewHistory(Long memberId, Long itemId) {
        if (memberId != null) {
            try {
                viewHistoryService.addViewedItem(memberId, itemId);
            } catch (Exception e) {
                log.warn("조회 이력 기록 실패 (무시됨) - memberId: {}, itemId: {}", memberId, itemId, e);
            }
        }
    }

    private Long getLatestOrderId(Long memberId, Long itemId) {
        if (memberId == null) {
            return null;
        }
        try {
            return orderService.findLatestOrderIdByMemberAndItem(memberId, itemId);
        } catch (Exception e) {
            log.warn("주문 정보 조회 실패 - memberId: {}, itemId: {}", memberId, itemId, e);
            return null;
        }
    }

    private boolean checkCanWriteReview(Long memberId, Long itemId) {
        if (memberId == null) {
            return false;
        }
        return !reviewRepository.existsByItemIdAndMemberId(itemId, memberId);
    }

    private void addItemListPageAttributes(
            Model model,
            Page<ItemResponseDto> itemPage,
            PaginationInfo paginationInfo,
            String gender,
            String sort,
            String subCategory) {

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", paginationInfo.getCurrentPage());
        model.addAttribute("totalPages", paginationInfo.getTotalPages());
        model.addAttribute("pageNumbers", paginationInfo.getPageNumbers());
        model.addAttribute("hasPrevBlock", paginationInfo.isHasPrevBlock());
        model.addAttribute("hasNextBlock", paginationInfo.isHasNextBlock());
        model.addAttribute("prevBlockPage", paginationInfo.getPrevBlockPage());
        model.addAttribute("nextBlockPage", paginationInfo.getNextBlockPage());
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedCategory", subCategory);

        // 카테고리 통계 추가
        try {
            CategoryStats categoryStats = categoryStatsService.getCategoryStats();
            model.addAttribute("categoryStats", categoryStats);
        } catch (Exception e) {
            log.warn("CategoryStats 조회 실패: {}", e.getMessage());
            model.addAttribute("categoryStats", new CategoryStats());
        }
    }

    private void addItemDetailAttributes(
            Model model,
            ItemResponseDto item,
            List<Review> reviews,
            Double averageRating,
            boolean canWriteReview,
            Long orderId) {

        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("canWriteReview", canWriteReview);
        model.addAttribute("orderId", orderId);
        model.addAttribute("isInStock", item.getQuantity() > 0);
        model.addAttribute("isLowStock", item.getQuantity() > 0 && item.getQuantity() <= 5);
        model.addAttribute("stockStatus", getStockStatus(item.getQuantity()));
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("hasReviews", !reviews.isEmpty());
    }

    private String getStockStatus(Integer quantity) {
        if (quantity == null || quantity <= 0) return "품절";
        if (quantity <= 5) return "품절 임박";
        if (quantity <= 10) return "재고 부족";
        return "재고 충분";
    }

//    private void addItemListPageRecommendations(Model model, CustomUserDetails userDetails) {
//        try {
//            Long memberId = getMemberId(userDetails);
//
//            // 개인화 추천
//            if (memberId != null) {
//                RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
//                        memberId, 4, "content_based"
//                );
//                if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
//                    model.addAttribute("quickRecommendations", personalRecs.getRecommendations());
//                }
//            }
//
//            // 인기 상품
//            Map<String, Object> popularItems = recommendationService.getPopularItems(6);
//            if (popularItems != null && Boolean.TRUE.equals(popularItems.get("success"))) {
//                model.addAttribute("listPagePopularItems", popularItems.get("recommendations"));
//            }
//
//        } catch (Exception e) {
//            log.warn("상품 목록 페이지 추천 실패", e);
//        }
//    }

//    private void addItemDetailRecommendations(
//            Model model,
//            Long memberId,
//            Long itemId,
//            ItemResponseDto currentItem) {
//
//        try {
//            // 유사 상품
//            Map<String, Object> similarItems = recommendationService.getSimilarItems(
//                    List.of(itemId.intValue()), 6
//            );
//            if (similarItems != null && Boolean.TRUE.equals(similarItems.get("success"))) {
//                model.addAttribute("similarItems", similarItems.get("recommendations"));
//            }
//
//            // 같은 카테고리 상품
//            if (currentItem.getCategory() != null) {
//                Map<String, Object> categoryItems = recommendationService.getCategoryRecommendations(
//                        String.valueOf(currentItem.getCategory()), 6
//                );
//                if (categoryItems != null && Boolean.TRUE.equals(categoryItems.get("success"))) {
//                    model.addAttribute("sameCategoryItems", categoryItems.get("recommendations"));
//                }
//            }
//
//            // 개인화 추천
//            if (memberId != null) {
//                RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
//                        memberId, 8, "hybrid"
//                );
//                if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
//                    model.addAttribute("detailPersonalizedItems", personalRecs.getRecommendations());
//                }
//            }
//
//        } catch (Exception e) {
//            log.warn("상품 상세 추천 실패 - itemId: {}", itemId, e);
//        }
//    }
//
//    private void recordUserInteraction(Long memberId, Long itemId, String action) {
//        if (memberId != null) {
//            try {
//                recommendationService.recordUserInteraction(memberId, itemId, action);
//                log.debug("사용자 상호작용 기록 - 사용자: {}, 상품: {}, 액션: {}", memberId, itemId, action);
//            } catch (Exception e) {
//                log.warn("사용자 상호작용 기록 실패 (무시됨)", e);
//            }
//        }
//    }
}