package zerobase.MyShoppingMall.controller.main;

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
import org.springframework.web.bind.annotation.RequestParam;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.dto.item.neo.PaginationInfo;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.service.item.PaginationService;
import zerobase.MyShoppingMall.temps.RecommendationResponse;
import zerobase.MyShoppingMall.temps.RecommendationService;
import zerobase.MyShoppingMall.type.Gender;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;
    private final PaginationService paginationService;
    private final RecommendationService recommendationService;

    /**
     * 메인 페이지 (홈페이지)
     * 상품 필터링,추천 기능 - 메인 랜딩 페이지
     */
    @GetMapping("/")
    public String mainPage(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "category", required = false) String subCategory,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        try {
            // 🔧 추가: 사용자 정보를 모델에 추가 (JavaScript에서 사용)
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);
            if (memberId != null) {
                model.addAttribute("currentUserId", memberId);
                model.addAttribute("isAuthenticated", true);
            } else {
                model.addAttribute("isAuthenticated", false);
            }

            // Gender enum 변환
            Gender genderEnum = parseGender(gender);

            // 페이지 크기 검증 및 보정
            int validatedSize = paginationService.validateAndCorrectPageSize(16);

            // 아이템 페이징 조회
            Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, subCategory, page, validatedSize);

            // 페이지네이션 정보 생성
            PaginationInfo paginationInfo = paginationService.createPaginationInfo(itemPage);

            // 기본 모델 데이터 추가
            addMainPageAttributesToModel(model, itemPage, paginationInfo, gender, sort, subCategory);

            // 🔧 수정: 추천 시스템 연동 (안전하게)
            addMainPageRecommendationsWithFallback(model, userDetails);

            log.info("메인 페이지 로드 완료 - 필터: gender={}, sort={}, category={}, page={}, 사용자: {}",
                    gender, sort, subCategory, page, memberId);

            return "mainPage";

        } catch (Exception e) {
            log.error("메인 페이지 로드 중 오류 발생", e);
            model.addAttribute("errorMessage", "메인 페이지를 불러오는 중 문제가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 검색 페이지 - URL: /search (완성된 검색 기능)
     */
    @GetMapping("/search")
    public String searchPage(
            @RequestParam(required = false) String query,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "12") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        try {
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            if (query != null && !query.trim().isEmpty()) {
                // 검색 쿼리 정리
                String cleanQuery = query.trim();
                model.addAttribute("searchQuery", cleanQuery);

                // 검색 결과 조회
                Pageable pageable = PageRequest.of(page, size);
                Page<ItemResponseDto> searchResults = itemService.searchItems(cleanQuery, sort, pageable);

                // 페이지네이션 정보 생성
                PaginationInfo paginationInfo = paginationService.createPaginationInfo(searchResults);

                // 검색 결과 모델에 추가
                model.addAttribute("searchResults", searchResults.getContent());
                model.addAttribute("totalResults", searchResults.getTotalElements());
                model.addAttribute("currentPage", paginationInfo.getCurrentPage());
                model.addAttribute("totalPages", paginationInfo.getTotalPages());
                model.addAttribute("pageNumbers", paginationInfo.getPageNumbers());
                model.addAttribute("hasPrevBlock", paginationInfo.isHasPrevBlock());
                model.addAttribute("hasNextBlock", paginationInfo.isHasNextBlock());
                model.addAttribute("prevBlockPage", paginationInfo.getPrevBlockPage());
                model.addAttribute("nextBlockPage", paginationInfo.getNextBlockPage());
                model.addAttribute("selectedSort", sort);

//                // 검색어 저장 (추천 시스템용)
//                if (memberId != null) {
//                    recordSearchQuery(memberId, cleanQuery);
//                }

                // 추천 시스템: 검색 결과 기반 추천
                addSearchPageRecommendations(model, memberId, cleanQuery);

                log.info("검색 완료 - 쿼리: {}, 결과: {}개", cleanQuery, searchResults.getTotalElements());
            } else {
                // 검색어가 없으면 인기 검색어와 추천 상품만 표시
                addPopularSearches(model);
                addMainPageRecommendations(model, userDetails);
            }

            return "search/results";

        } catch (Exception e) {
            log.error("검색 페이지 오류 - 쿼리: {}", query, e);
            model.addAttribute("errorMessage", "검색 중 문제가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 카테고리별 상품 페이지 - URL: /category/{categoryName}
     */
    @GetMapping("/category/{categoryName}")
    public String categoryPage(
            @PathVariable String categoryName,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "gender", required = false) String gender,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        try {
            Gender genderEnum = parseGender(gender);
            int validatedSize = paginationService.validateAndCorrectPageSize(16);

            // 카테고리별 상품 조회
            Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, categoryName, page, validatedSize);
            PaginationInfo paginationInfo = paginationService.createPaginationInfo(itemPage);

            // 모델 데이터 추가
            model.addAttribute("items", itemPage.getContent());
            model.addAttribute("categoryName", categoryName);
            model.addAttribute("currentPage", paginationInfo.getCurrentPage());
            model.addAttribute("totalPages", paginationInfo.getTotalPages());
            model.addAttribute("pageNumbers", paginationInfo.getPageNumbers());
            model.addAttribute("hasPrevBlock", paginationInfo.isHasPrevBlock());
            model.addAttribute("hasNextBlock", paginationInfo.isHasNextBlock());
            model.addAttribute("prevBlockPage", paginationInfo.getPrevBlockPage());
            model.addAttribute("nextBlockPage", paginationInfo.getNextBlockPage());
            model.addAttribute("selectedGender", gender);
            model.addAttribute("selectedSort", sort);

            // 카테고리별 추천
            addCategorySpecificRecommendations(model, categoryName, userDetails);

            return "category/list";

        } catch (Exception e) {
            log.error("카테고리 페이지 오류 - 카테고리: {}", categoryName, e);
            model.addAttribute("errorMessage", "카테고리 페이지를 불러오는 중 문제가 발생했습니다.");
            return "error/500";
        }
    }

    // === 추천 시스템 관련 메서드 ===

    /**
     * 메인 페이지 추천 상품 추가
     */
    private void addMainPageRecommendations(Model model, CustomUserDetails userDetails) {
        try {
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            if (memberId != null) {
                // 🔧 수정: 로그인 사용자 - 개인화 추천
                log.info("개인화 추천 조회 시작 - 사용자: {}", memberId);

                try {
                    RecommendationResponse personalizedRecs = recommendationService.getSafeRecommendations(
                            memberId, 8, "hybrid"
                    );

                    if (personalizedRecs.isSuccess() && personalizedRecs.getRecommendations() != null) {
                        model.addAttribute("personalizedItems", personalizedRecs.getRecommendations());
                        log.info("개인화 추천 성공 - {}개", personalizedRecs.getRecommendations().size());
                    } else {
                        log.warn("개인화 추천 실패, 인기 상품으로 대체");
                        addFallbackPopularItems(model, 8);
                    }
                } catch (Exception e) {
                    log.warn("개인화 추천 예외 발생, 인기 상품으로 대체", e);
                    addFallbackPopularItems(model, 8);
                }

                // 🔧 수정: 실시간 추천 (최근 조회 기반)
                try {
                    Map<String, Object> realtimeRecs = recommendationService.getRealTimeRecommendations(
                            memberId, null, null, 6
                    );

                    if (realtimeRecs != null && (Boolean) realtimeRecs.getOrDefault("success", false)) {
                        model.addAttribute("realtimeItems", realtimeRecs.get("recommendations"));
                        log.info("실시간 추천 성공");
                    } else {
                        log.warn("실시간 추천 실패");
                    }
                } catch (Exception e) {
                    log.warn("실시간 추천 예외 발생", e);
                }

            } else {
                // 🔧 수정: 비로그인 사용자 - 인기 상품
                log.info("비로그인 사용자 - 인기 상품 조회");
                addFallbackPopularItems(model, 8);
            }

            // 🔧 수정: 카테고리별 추천 (안전하게 처리)
            addSafeCategoryRecommendations(model);

            log.info("메인 페이지 추천 추가 완료");

        } catch (Exception e) {
            log.error("메인 페이지 추천 추가 중 심각한 오류 발생", e);
            // 최소한의 데이터라도 표시되도록
            addFallbackPopularItems(model, 8);
        }
    }

    private void addFallbackPopularItems(Model model, int count) {
        try {
            // 추천 시스템이 실패한 경우 ItemService에서 직접 조회
            Page<ItemResponseDto> popularItems = itemService.findItems(null, "popular", null, 0, count);

            if (popularItems != null && popularItems.hasContent()) {
                model.addAttribute("popularItems", popularItems.getContent());
                log.info("Fallback 인기 상품 추가 완료 - {}개", popularItems.getContent().size());
            } else {
                log.warn("Fallback 인기 상품도 조회 실패");
            }
        } catch (Exception e) {
            log.error("Fallback 인기 상품 조회 실패", e);
        }
    }

    private void addSafeCategoryRecommendations(Model model) {
        String[] categories = {"상의", "하의", "아우터", "신발", "액세서리"};

        for (String category : categories) {
            try {
                // 추천 시스템 시도
                Map<String, Object> recommendedItems = recommendationService.getCategoryRecommendations(category, 4);

                if (recommendedItems != null && (Boolean) recommendedItems.getOrDefault("success", false)) {
                    List<Object> recommendations = (List<Object>) recommendedItems.get("recommendations");
                    if (recommendations != null && !recommendations.isEmpty()) {
                        model.addAttribute(category + "Items", recommendations);
                        log.debug("카테고리 추천 성공 - {}: {}개", category, recommendations.size());
                        continue;
                    }
                }

                // 추천 실패 시 fallback
                log.debug("카테고리 추천 실패, ItemService로 대체 - 카테고리: {}", category);

                String subCategoryMapping = mapCategoryToSubCategory(category);
                Page<ItemResponseDto> fallbackItems = itemService.findItems(null, "popular", subCategoryMapping, 0, 4);

                if (fallbackItems != null && fallbackItems.hasContent()) {
                    model.addAttribute(category + "Items", fallbackItems.getContent());
                    log.debug("카테고리 Fallback 성공 - {}: {}개", category, fallbackItems.getContent().size());
                }

            } catch (Exception e) {
                log.warn("카테고리 추천 완전 실패 - 카테고리: {}", category, e);
            }
        }
    }


    private String mapCategoryToSubCategory(String category) {
        switch (category) {
            case "상의":
                return "tshirt"; // 또는 실제 DB의 sub_category 값
            case "하의":
                return "jeans";
            case "아우터":
                return "coat";
            case "신발":
                return "sneakers";
            case "액세서리":
                return "watch";
            default:
                return null;
        }
    }


    /**
     * 검색 페이지 추천 (완성된 버전)
     */
    private void addSearchPageRecommendations(Model model, Long memberId, String query) {
        try {
            // 1. 검색어 기반 관련 상품 추천
            Map<String, Object> relatedItems = recommendationService.getSearchRelatedRecommendations(query, 8);
            if (relatedItems != null && relatedItems.get("recommendations") != null) {
                model.addAttribute("searchRelatedItems", relatedItems.get("recommendations"));
            }

            // 2. 개인화 추천 (로그인 시)
            if (memberId != null) {
                try {
                    RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
                            memberId, 8, "hybrid"
                    );
                    if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
                        model.addAttribute("searchPersonalizedItems", personalRecs.getRecommendations());
                    }
                } catch (Exception e) {
                    log.warn("검색 페이지 개인화 추천 실패", e);
                }
            }

            // 3. 인기 검색 상품
            Map<String, Object> popularItems = recommendationService.getPopularItems(6);
            if (popularItems != null && (Boolean) popularItems.getOrDefault("success", false)) {
                model.addAttribute("searchPopularItems", popularItems.get("recommendations"));
            }

            // 4. 관련 검색어 추가
            List<String> relatedQueries = generateRelatedQueries(query);
            model.addAttribute("relatedQueries", relatedQueries);

            log.debug("검색 페이지 추천 완료 - 쿼리: {}", query);

        } catch (Exception e) {
            log.warn("검색 페이지 추천 실패 - 쿼리: {}", query, e);
        }
    }

    /**
     * 카테고리별 추천 추가
     */
    private void addCategoryRecommendations(Model model) {
        String[] categories = {"상의", "하의", "아우터", "신발", "액세서리"};

        for (String category : categories) {
            try {
                Map<String, Object> categoryItems = recommendationService.getCategoryRecommendations(
                        category, 4
                );
                model.addAttribute(category + "Items", categoryItems.get("recommendations"));
            } catch (Exception e) {
                log.warn("카테고리 추천 실패 - 카테고리: {}", category, e);
            }
        }
    }

    /**
     * 카테고리별 특화 추천
     */
    private void addCategorySpecificRecommendations(Model model, String categoryName, CustomUserDetails userDetails) {
        try {
            Long memberId = getUserMemberIdIfAuthenticated(userDetails);

            // 해당 카테고리 인기 상품
            Map<String, Object> categoryPopular = recommendationService.getCategoryRecommendations(categoryName, 8);
            if (categoryPopular != null && (Boolean) categoryPopular.getOrDefault("success", false)) {
                model.addAttribute("categoryPopularItems", categoryPopular.get("recommendations"));
            }

            // 개인화 추천 (로그인 시)
            if (memberId != null) {
                try {
                    RecommendationResponse personalRecs = recommendationService.getSafeRecommendations(
                            memberId, 6, "content_based"
                    );
                    if (personalRecs.isSuccess() && personalRecs.getRecommendations() != null) {
                        model.addAttribute("categoryPersonalizedItems", personalRecs.getRecommendations());
                    }
                } catch (Exception e) {
                    log.warn("카테고리 개인화 추천 실패", e);
                }
            }

        } catch (Exception e) {
            log.warn("카테고리 특화 추천 실패 - 카테고리: {}", categoryName, e);
        }
    }

    /**
     * 인기 검색어 추가
     */
    private void addPopularSearches(Model model) {
        try {
            List<String> popularSearches = List.of(
                    "후드티", "청바지", "운동화", "패딩", "맨투맨",
                    "코트", "스니커즈", "바람막이", "시계", "반지"
            );
            model.addAttribute("popularSearches", popularSearches);
        } catch (Exception e) {
            log.warn("인기 검색어 추가 실패", e);
        }
    }

    /**
     * 검색어 기록 (추천 시스템용)
     */
//    private void recordSearchQuery(Long memberId, String query) {
//        try {
//            recommendationService.recordUserInteraction(memberId, null, "search", query);
//            log.debug("검색어 기록 완료 - 사용자: {}, 검색어: {}", memberId, query);
//        } catch (Exception e) {
//            log.warn("검색어 기록 실패 - 사용자: {}, 검색어: {}", memberId, query, e);
//        }
//    }

    /**
     * 관련 검색어 생성
     */
    private List<String> generateRelatedQueries(String query) {
        // 관련 검색어 맵핑
        String lowerQuery = query.toLowerCase();

        if (lowerQuery.contains("후드") || lowerQuery.contains("hoodie")) {
            return List.of("맨투맨", "스웨트셔츠", "니트", "카디건");
        } else if (lowerQuery.contains("바지") || lowerQuery.contains("팬츠")) {
            return List.of("청바지", "슬랙스", "조거팬츠", "트레이닝팬츠");
        } else if (lowerQuery.contains("신발") || lowerQuery.contains("shoes")) {
            return List.of("운동화", "스니커즈", "구두", "부츠");
        } else if (lowerQuery.contains("패딩") || lowerQuery.contains("다운")) {
            return List.of("코트", "자켓", "점퍼", "바람막이");
        } else {
            return List.of("인기상품", "신상품", "세일상품", "추천상품");
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
     * 메인 페이지 모델 속성 추가
     */
    private void addMainPageAttributesToModel(Model model,
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
    }

    /**
     * 🔧 추가: 안전한 메인 페이지 추천 (fallback 포함)
     */
    private void addMainPageRecommendationsWithFallback(Model model, CustomUserDetails userDetails) {
        Long memberId = getUserMemberIdIfAuthenticated(userDetails);
        boolean recommendationSystemWorking = false;

        try {
            // 추천 시스템 상태 확인
            recommendationSystemWorking = recommendationService.isHealthy();
            log.info("추천 시스템 상태: {}", recommendationSystemWorking ? "정상" : "비정상");

        } catch (Exception e) {
            log.warn("추천 시스템 상태 확인 실패", e);
        }

        if (recommendationSystemWorking) {
            // 추천 시스템이 정상 작동하는 경우
            addMainPageRecommendations(model, userDetails);
        } else {
            // 추천 시스템이 작동하지 않는 경우 - 기본 데이터로 대체
            log.info("추천 시스템 비활성, 기본 상품 데이터 사용");
            addFallbackRecommendations(model, memberId);
        }

        // 추천 시스템 상태를 모델에 추가
        model.addAttribute("recommendationSystemActive", recommendationSystemWorking);
    }

    /**
     * 🔧 추가: Fallback 추천 (추천 시스템이 작동하지 않을 때)
     */
    private void addFallbackRecommendations(Model model, Long memberId) {
        try {
            // 1. 최신 상품
            Page<ItemResponseDto> latestItems = itemService.findItems(null, "latest", null, 0, 8);
            if (latestItems.hasContent()) {
                model.addAttribute("personalizedItems", latestItems.getContent());
            }

            // 2. 인기 상품 (가능한 경우)
            Page<ItemResponseDto> popularItems = itemService.findItems(null, "popular", null, 0, 8);
            if (popularItems.hasContent()) {
                model.addAttribute("popularItems", popularItems.getContent());
            }

            // 3. 카테고리별 기본 상품
            String[] categories = {"tshirt", "jeans", "sneakers", "coat"};
            for (String category : categories) {
                Page<ItemResponseDto> categoryItems = itemService.findItems(null, "popular", category, 0, 4);
                if (categoryItems.hasContent()) {
                    // 카테고리명을 한글로 변환
                    String koreanCategory = mapSubCategoryToKorean(category);
                    model.addAttribute(koreanCategory + "Items", categoryItems.getContent());
                }
            }

            log.info("Fallback 추천 데이터 추가 완료");

        } catch (Exception e) {
            log.error("Fallback 추천 데이터 추가 실패", e);
        }
    }

    /**
     * 🔧 추가: 서브카테고리를 한글로 매핑
     */
    private String mapSubCategoryToKorean(String subCategory) {
        switch (subCategory) {
            case "tshirt":
            case "hoodie":
            case "sweatshirt":
                return "상의";
            case "jeans":
            case "jogger_pants":
            case "training_pants":
                return "하의";
            case "coat":
            case "padding":
            case "windbreaker":
                return "아우터";
            case "sneakers":
            case "running_shoes":
            case "boots":
                return "신발";
            case "watch":
            case "ring":
            case "necklace":
                return "액세서리";
            default:
                return "기타";
        }
    }
}