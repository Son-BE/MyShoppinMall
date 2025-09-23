package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import zerobase.MyShoppingMall.temps.RecommendationResponse;
import zerobase.MyShoppingMall.temps.RecommendationService;
import zerobase.MyShoppingMall.type.Gender;

import java.util.List;
import java.util.Map;

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
    private final RecommendationService recommendationService;
    private final CategoryStatsService categoryStatsService;


    @GetMapping
    public String getItemsListPage(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "category", required = false) String subCategory,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        try {
            Gender genderEnum = parseGender(gender);
            int validatedSize = paginationService.validateAndCorrectPageSize(16);
            Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, subCategory, page, validatedSize);
            PaginationInfo paginationInfo = paginationService.createPaginationInfo(itemPage);
            addItemListPageAttributesToModel(model, itemPage, paginationInfo, gender, sort, subCategory);
            addItemListPageRecommendations(model, userDetails);

            log.info("상품 목록 페이지 로드 완료 - 필터: gender={}, sort={}, category={}, page={}",
                    gender, sort, subCategory, page);

            return "items/list";

        } catch (Exception e) {
            log.error("상품 목록 조회 중 오류 발생", e);
            model.addAttribute("errorMessage", "상품 목록을 불러오는 중 문제가 발생했습니다.");
            return "error/500";
        }
    }

    @GetMapping("/detail/{id}")
    public String getItemDetail(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        try {
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            if (memberId != null) {
                viewHistoryService.addViewedItem(memberId, id);
                Long orderId = orderService.findLatestOrderIdByMemberAndItem(memberId, id);
                model.addAttribute("orderId", orderId);
            } else {
                model.addAttribute("orderId", null);
            }

            if (memberId != null) {
                recordUserInteractionSafely(memberId, id, "view");
            }

            ItemResponseDto item = itemService.getItemWithCache(id, memberId);
            itemService.increaseViewCount(id);

            if (item == null) {
                model.addAttribute("errorMessage", "해당 상품을 찾을 수 없습니다.");
                return "error/404";
            }

            List<Review> reviews = reviewRepository.findByItemIdOrderByCreatedAtDesc(id);
            Double averageRating = reviewService.getAverageRating(id);

            boolean canWriteReview = checkCanWriteReview(memberId, id);

            addItemDetailAttributesToModel(model, item, reviews, averageRating, canWriteReview);
            addItemDetailPageRecommendations(model, memberId, id, item);

            log.info("아이템 상세 페이지 로드 완료 - itemId: {}, memberId: {}", id, memberId);
            return "user/detail";

        } catch (Exception e) {
            log.error("아이템 상세 정보 조회 중 오류 발생 - itemId: {}", id, e);
            model.addAttribute("errorMessage", "상품 상세 정보를 불러오는 중 문제가 발생했습니다.");
            return "error/500";
        }
    }

    private void addItemListPageRecommendations(Model model, CustomUserDetails userDetails) {
        try {
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            if (memberId != null) {
                RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
                        memberId, 4, "content_based"
                );
                if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
                    model.addAttribute("quickRecommendations", personalRecs.getRecommendations());
                }
            }
            Map<String, Object> popularItems = recommendationService.getPopularItems(6);
            if (popularItems != null && (Boolean) popularItems.getOrDefault("success", false)) {
                model.addAttribute("listPagePopularItems", popularItems.get("recommendations"));
            }

        } catch (Exception e) {
            log.warn("상품 목록 페이지 추천 실패", e);
        }
    }


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


    private Long getUserMemberIdIfAuthenticated(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getMember().getId() : null;
    }


    private boolean checkCanWriteReview(Long memberId, Long itemId) {
        if (memberId == null) {
            return true;
        }
        return !reviewRepository.existsByItemIdAndMemberId(itemId, memberId);
    }

    private void addItemListPageAttributesToModel(Model model,
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

        try {
            CategoryStats categoryStats = categoryStatsService.getCategoryStats();
            model.addAttribute("categoryStats", categoryStats);
            log.debug("CategoryStats 추가 완료");
        } catch (Exception e) {
            log.warn("CategoryStats 조회 실패, 기본값 설정: {}", e.getMessage());
            model.addAttribute("categoryStats", createDefaultCategoryStats());
        }
    }

    private CategoryStats createDefaultCategoryStats() {
        CategoryStats defaultStats = new CategoryStats();
        defaultStats.setChartData("{}");
        return defaultStats;
    }


    private void addItemDetailAttributesToModel(Model model,
                                                ItemResponseDto item,
                                                List<Review> reviews,
                                                Double averageRating,
                                                boolean canWriteReview) {
        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("canWriteReview", canWriteReview);

        model.addAttribute("isInStock", item.getQuantity() > 0);
        model.addAttribute("isLowStock", item.getQuantity() > 0 && item.getQuantity() <= 5);
        model.addAttribute("stockStatus", getStockStatus(item.getQuantity()));

        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("hasReviews", !reviews.isEmpty());
    }


    private String getStockStatus(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return "품절";
        } else if (quantity <= 5) {
            return "품절 임박";
        } else if (quantity <= 10) {
            return "재고 부족";
        } else {
            return "재고 충분";
        }
    }

    private void addItemDetailPageRecommendations(Model model, Long memberId, Long itemId, ItemResponseDto currentItem) {
        try {
            List<Integer> currentItemList = List.of(itemId.intValue());
            Map<String, Object> similarItems = recommendationService.getSimilarItems(currentItemList, 6);

            if (similarItems != null && (Boolean) similarItems.getOrDefault("success", false)) {
                model.addAttribute("similarItems", similarItems.get("recommendations"));
                log.debug("유사 상품 추천 추가 완료 - 기준 상품: {}", itemId);
            }
            if (currentItem.getCategory() != null) {
                Map<String, Object> categoryItems = recommendationService.getCategoryRecommendations(
                        String.valueOf(currentItem.getCategory()), 6
                );
                if (categoryItems != null && (Boolean) categoryItems.getOrDefault("success", false)) {
                    model.addAttribute("sameCategoryItems", categoryItems.get("recommendations"));
                }
            }
            if (memberId != null) {
                RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
                        memberId, 8, "hybrid"
                );
                if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
                    model.addAttribute("detailPersonalizedItems", personalRecs.getRecommendations());
                }
            }

        } catch (Exception e) {
            log.warn("상품 상세 페이지 추천 추가 실패 - itemId: {}", itemId, e);
        }
    }

    private void recordUserInteractionSafely(Long memberId, Long itemId, String action) {
        try {
            recommendationService.recordUserInteraction(memberId, itemId, action);
            log.debug("사용자 상호작용 기록 성공 - 사용자: {}, 상품: {}, 액션: {}", memberId, itemId, action);
        } catch (Exception e) {
            log.warn("사용자 상호작용 기록 실패 (무시됨) - 사용자: {}, 상품: {}, 액션: {}",
                    memberId, itemId, action, e);
        }
    }


}


