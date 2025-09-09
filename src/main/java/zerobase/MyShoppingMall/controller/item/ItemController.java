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

    /**
     * 상품 목록 페이지 - URL: /items
     * (카테고리별, 필터별 상품 목록 조회)
     */
    @GetMapping
    public String getItemsListPage(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "category", required = false) String subCategory,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        try {
            // Gender enum 변환
            Gender genderEnum = parseGender(gender);

            // 페이지 크기 검증 및 보정
            int validatedSize = paginationService.validateAndCorrectPageSize(16);

            // 아이템 페이징 조회
            Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, subCategory, page, validatedSize);

            // 페이지네이션 정보 생성
            PaginationInfo paginationInfo = paginationService.createPaginationInfo(itemPage);

            // 기본 모델 데이터 추가
            addItemListPageAttributesToModel(model, itemPage, paginationInfo, gender, sort, subCategory);

            //  추천 시스템: 상품 목록 페이지 추천 (개인화는 제한적)
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

    /**
     * 🔍 상품 상세 페이지 - URL: /items/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public String getItemDetail(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        try {
            // 사용자 인증 처리
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            // 조회 히스토리 및 주문 정보 처리
            if (memberId != null) {
                viewHistoryService.addViewedItem(memberId, id);
                Long orderId = orderService.findLatestOrderIdByMemberAndItem(memberId, id);
                model.addAttribute("orderId", orderId);
            } else {
                model.addAttribute("orderId", null);
            }

            // 🔧 수정: 추천 시스템 상호작용 기록 (안전하게)
            if (memberId != null) {
                recordUserInteractionSafely(memberId, id, "view");
            }

            // 아이템 정보 조회 및 조회수 증가
            ItemResponseDto item = itemService.getItemWithCache(id, memberId);
            itemService.increaseViewCount(id);

            // 아이템 유효성 검사
            if (item == null) {
                model.addAttribute("errorMessage", "해당 상품을 찾을 수 없습니다.");
                return "error/404";
            }

            // 리뷰 정보 조회
            List<Review> reviews = reviewRepository.findByItemIdOrderByCreatedAtDesc(id);
            Double averageRating = reviewService.getAverageRating(id);

            // 리뷰 작성 가능 여부
            boolean canWriteReview = checkCanWriteReview(memberId, id);

            // 기본 모델 데이터
            addItemDetailAttributesToModel(model, item, reviews, averageRating, canWriteReview);

            // 🔧 추가: 상품 상세 페이지 추천 추가
            addItemDetailPageRecommendations(model, memberId, id, item);

            log.info("아이템 상세 페이지 로드 완료 - itemId: {}, memberId: {}", id, memberId);
            return "user/detail";

        } catch (Exception e) {
            log.error("아이템 상세 정보 조회 중 오류 발생 - itemId: {}", id, e);
            model.addAttribute("errorMessage", "상품 상세 정보를 불러오는 중 문제가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 상품 목록 페이지 추천 (메인 페이지보다 제한적)
     */
    private void addItemListPageRecommendations(Model model, CustomUserDetails userDetails) {
        try {
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            // 간단한 추천만 제공 (성능 고려)
            if (memberId != null) {
                // 개인화 추천 (소량)
                RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
                        memberId, 4, "content_based"
                );
                if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
                    model.addAttribute("quickRecommendations", personalRecs.getRecommendations());
                }
            }

            // 인기 상품 (공통)
            Map<String, Object> popularItems = recommendationService.getPopularItems(6);
            if (popularItems != null && (Boolean) popularItems.getOrDefault("success", false)) {
                model.addAttribute("listPagePopularItems", popularItems.get("recommendations"));
            }

        } catch (Exception e) {
            log.warn("상품 목록 페이지 추천 실패", e);
        }
    }


    /**
     * 사용자 상호작용 기록 (안전하게 처리)
     */
    private void recordUserInteraction(Long memberId, Long itemId, String action) {
        try {
            recommendationService.recordUserInteraction(memberId, itemId, action);
            log.debug("사용자 상호작용 기록 - 사용자: {}, 상품: {}, 액션: {}", memberId, itemId, action);
        } catch (Exception e) {
            log.warn("사용자 상호작용 기록 실패 - 사용자: {}, 상품: {}, 액션: {}",
                    memberId, itemId, action, e);
        }
    }

    // === Helper Methods ===

    /**
     * Gender 문자열을 enum으로 변환
     */
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

    /**
     * 인증된 사용자의 memberId 반환
     */
    private Long getUserMemberIdIfAuthenticated(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getMember().getId() : null;
    }

    /**
     * 리뷰 작성 가능 여부 확인
     */
    private boolean checkCanWriteReview(Long memberId, Long itemId) {
        if (memberId == null) {
            return true; // 비로그인 사용자는 일단 true (로그인 시 체크)
        }
        return !reviewRepository.existsByItemIdAndMemberId(itemId, memberId);
    }

    /**
     * 상품 목록 페이지 모델 속성 추가
     */
    private void addItemListPageAttributesToModel(Model model,
                                                  Page<ItemResponseDto> itemPage,
                                                  PaginationInfo paginationInfo,
                                                  String gender,
                                                  String sort,
                                                  String subCategory) {
        // 아이템 데이터
        model.addAttribute("items", itemPage.getContent());

        // 페이지네이션 정보
        model.addAttribute("currentPage", paginationInfo.getCurrentPage());
        model.addAttribute("totalPages", paginationInfo.getTotalPages());
        model.addAttribute("pageNumbers", paginationInfo.getPageNumbers());
        model.addAttribute("hasPrevBlock", paginationInfo.isHasPrevBlock());
        model.addAttribute("hasNextBlock", paginationInfo.isHasNextBlock());
        model.addAttribute("prevBlockPage", paginationInfo.getPrevBlockPage());
        model.addAttribute("nextBlockPage", paginationInfo.getNextBlockPage());

        // 선택된 필터 정보
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
        defaultStats.setChartData("{}"); // 빈 JSON 객체
        return defaultStats;
    }

    /**
     * 아이템 상세 페이지 모델 속성 추가
     */
    private void addItemDetailAttributesToModel(Model model,
                                                ItemResponseDto item,
                                                List<Review> reviews,
                                                Double averageRating,
                                                boolean canWriteReview) {
        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        model.addAttribute("canWriteReview", canWriteReview);

        // 추가 상품 정보
        model.addAttribute("isInStock", item.getQuantity() > 0);
        model.addAttribute("isLowStock", item.getQuantity() > 0 && item.getQuantity() <= 5);
        model.addAttribute("stockStatus", getStockStatus(item.getQuantity()));

        // 리뷰 통계
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("hasReviews", !reviews.isEmpty());
    }

    /**
     * 재고 상태 반환
     */
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
            // 1. 유사 상품 추천
            List<Integer> currentItemList = List.of(itemId.intValue());
            Map<String, Object> similarItems = recommendationService.getSimilarItems(currentItemList, 6);

            if (similarItems != null && (Boolean) similarItems.getOrDefault("success", false)) {
                model.addAttribute("similarItems", similarItems.get("recommendations"));
                log.debug("유사 상품 추천 추가 완료 - 기준 상품: {}", itemId);
            }

            // 2. 같은 카테고리 추천
            if (currentItem.getCategory() != null) {
                Map<String, Object> categoryItems = recommendationService.getCategoryRecommendations(
                        String.valueOf(currentItem.getCategory()), 6
                );
                if (categoryItems != null && (Boolean) categoryItems.getOrDefault("success", false)) {
                    model.addAttribute("sameCategoryItems", categoryItems.get("recommendations"));
                }
            }

            // 3. 개인화 추천 (로그인 시)
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


