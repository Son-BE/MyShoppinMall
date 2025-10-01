package zerobase.MyShoppingMall.service.recommend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.entity.Item;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recommend")
@Slf4j
public class RecommendController {

    private final NlpRecommendationService nlpRecommendationService;

    @GetMapping("/nlp")
    public String nlpRecommend(@RequestParam("query") String query,
                               @RequestParam(value = "userId", required = false) Long userId,
                               Model model) {
        log.info("NLP 추천 요청: {} (사용자: {})", query, userId);

        try {
            // 1. 입력 검증
            if (query == null || query.trim().isEmpty()) {
                model.addAttribute("error", "검색어를 입력해주세요.");
                return "fragments/ai-recommend-error";
            }

            // 2. 사용자 컨텍스트 구성
            Map<String, Object> userContext = buildUserContext(userId);

            // 3. Python NLP 서버를 통한 추천 실행
            NlpRecommendationService.RecommendationResult result = nlpRecommendationService.recommendByNaturalLanguage(
                    query, 4, userContext
            );

            if (!result.isSuccess() || result.getItems().isEmpty()) {
                model.addAttribute("query", query);
                model.addAttribute("message", "'" + query + "'에 대한 AI 추천 상품을 찾을 수 없습니다.");
                model.addAttribute("suggestions", generateSearchSuggestions(query));
                return "fragments/ai-recommend-empty";
            }

            // 4. UI용 DTO 변환
            List<RecommendItemDto> recommendDtos = result.getItems().stream()
                    .map(item -> RecommendItemDto.builder()
                            .id(item.getId())
                            .itemName(item.getItemName())
                            .price(item.getPrice())
                            .imagePath(item.getImageUrl())
                            .category(String.valueOf(item.getCategory()))
                            .style(String.valueOf(item.getStyle()))
                            .recommendationReason(result.getReasons().get(item.getId()))
                            .itemRating((double) item.getItemRating())
                            .aiScore(calculateDisplayScore(result.getConfidence(), item))
                            .semanticMatch(result.getConfidence() > 0.7)
                            .build())
                    .collect(Collectors.toList());

            // 5. 모델에 데이터 추가
            model.addAttribute("query", query);
            model.addAttribute("intent", translateIntent(result.getIntent()));
            model.addAttribute("recommendedItems", recommendDtos);
            model.addAttribute("totalCount", recommendDtos.size());
            model.addAttribute("confidence", Math.round(result.getConfidence() * 100));
            model.addAttribute("aiModel", result.getModel());
            model.addAttribute("processingTime", result.getProcessingTime());
            model.addAttribute("keywords", extractKeywordsFromResult(result));
            model.addAttribute("hasHighConfidence", result.getConfidence() > 0.8);

            return "fragments/ai-recommend-result";

        } catch (Exception e) {
            log.error("NLP 추천 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "AI 추천 시스템에 일시적인 문제가 발생했습니다.");
            model.addAttribute("query", query);
            model.addAttribute("isSystemError", true);
            return "fragments/ai-recommend-error";
        }
    }

    /**
     * API용 자연어 추천
     */
    @PostMapping("/api/nlp")
    @ResponseBody
    public ResponseEntity<?> nlpRecommendApi(@RequestBody NLPRecommendRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            // 입력 검증
            if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "검색어를 입력해주세요.");
                errorResponse.put("errorCode", "INVALID_QUERY");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 사용자 컨텍스트 구성
            Map<String, Object> userContext = Optional.ofNullable(request.getUserContext())
                    .orElse(new HashMap<>());

            int limit = Math.max(1, Math.min(request.getLimit(), 50));

            // Python NLP 서버를 통한 추천
            NlpRecommendationService.RecommendationResult result = nlpRecommendationService.recommendByNaturalLanguage(
                    request.getQuery(), limit, userContext
            );

            // API 응답 구성
            List<Map<String, Object>> itemsData = result.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("itemName", item.getItemName());
                        itemMap.put("price", item.getPrice());
                        itemMap.put("imagePath", item.getImageUrl());
                        itemMap.put("category", item.getCategory());
                        itemMap.put("style", item.getStyle());
                        itemMap.put("rating", item.getItemRating());
                        itemMap.put("reviewCount", item.getReviewCount());
                        itemMap.put("reason", result.getReasons().get(item.getId()));
                        itemMap.put("aiScore", calculateDisplayScore(result.getConfidence(), item));
                        itemMap.put("semanticMatch", result.getConfidence() > 0.7);
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            long endTime = System.currentTimeMillis();

            // HashMap을 사용하여 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("query", request.getQuery());
            response.put("intent", result.getIntent());
            response.put("intentKorean", translateIntent(result.getIntent()));
            response.put("count", itemsData.size());
            response.put("items", itemsData);
            response.put("confidence", result.getConfidence());
            response.put("model", result.getModel());
            response.put("processingTime", endTime - startTime);
            response.put("totalCandidates", result.getTotalCandidates());
            response.put("nlpServiceStatus", nlpRecommendationService.isNLPServiceHealthy());
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("API NLP 추천 오류: {}", e.getMessage(), e);
            long endTime = System.currentTimeMillis();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "AI 추천 시스템 오류: " + e.getMessage());
            errorResponse.put("errorCode", "NLP_SERVICE_ERROR");
            errorResponse.put("query", request.getQuery());
            errorResponse.put("processingTime", endTime - startTime);
            errorResponse.put("timestamp", new Date());

            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * 추천 피드백 수집
     */
    @PostMapping("/api/feedback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> collectFeedback(@RequestBody RecommendationFeedback feedback) {
        try {
            log.info("추천 피드백 수집: 쿼리={}, 만족도={}, 클릭된상품={}",
                    feedback.getQuery(), feedback.getSatisfaction(), feedback.getClickedItems());


            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "피드백이 성공적으로 수집되었습니다.");
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "피드백 처리 중 오류 발생: " + e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 추천 시스템 상태 확인
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            // NLP 서비스 상태 확인
            boolean nlpServiceReady = nlpRecommendationService.isNLPServiceHealthy();

            health.put("status", nlpServiceReady ? "healthy" : "degraded");
            health.put("recommendation_service", true);
            health.put("nlp_service", nlpServiceReady);
            health.put("python_backend", nlpServiceReady);
            health.put("model", "Python-E5-Korean + Java-Integration");
            health.put("timestamp", new Date());
            health.put("version", "3.0.0-Hybrid");

            // features Map 별도 생성
            Map<String, Object> features = new HashMap<>();
            features.put("korean_nlp", true);
            features.put("semantic_search", nlpServiceReady);
            features.put("intent_classification", nlpServiceReady);
            features.put("entity_extraction", nlpServiceReady);
            features.put("fallback_system", true);
            features.put("caching", true);

            health.put("features", features);

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "error");
            health.put("error", e.getMessage());
            health.put("timestamp", new Date());
            return ResponseEntity.status(500).body(health);
        }
    }

    /**
     * 검색 제안 생성
     */
    @GetMapping("/api/suggestions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(@RequestParam("query") String query) {
        try {
            List<String> suggestions = generateSearchSuggestions(query);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", query);
            response.put("suggestions", suggestions);
            response.put("timestamp", new Date());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // === 유틸리티 메서드들 ===

    private Map<String, Object> buildUserContext(Long userId) {
        Map<String, Object> context = new HashMap<>();

        if (userId != null) {
            context.put("userId", userId);
            // TODO: 사용자 선호도, 구매 이력 등 추가
        }

        // 현재 계절 정보
        context.put("currentSeason", getCurrentSeason());
        context.put("timestamp", new Date());

        return context;
    }

    private String getCurrentSeason() {
        int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
        if (month >= 3 && month <= 5) return "SPRING";
        else if (month >= 6 && month <= 8) return "SUMMER";
        else if (month >= 9 && month <= 11) return "AUTUMN";
        else return "WINTER";
    }

    private String translateIntent(String intent) {
        Map<String, String> intentMap = new HashMap<>();
        intentMap.put("style_recommendation", "스타일 추천");
        intentMap.put("product_search", "상품 검색");
        intentMap.put("occasion_based", "상황별 추천");
        intentMap.put("style_advice", "스타일 조언");
        intentMap.put("general_search", "일반 검색");
        intentMap.put("fallback_search", "키워드 검색");

        return intentMap.getOrDefault(intent, "상품 추천");
    }

    private Double calculateDisplayScore(double confidence, Item item) {
        double baseScore = confidence * 60; // 시맨틱 매칭 점수

        // 상품 품질 점수
        if (item.getItemRating() > 0) {
            baseScore += (item.getItemRating() / 5.0) * 25;
        }

        // 인기도 점수
        if (item.getReviewCount() != null && item.getReviewCount() > 0) {
            double popularityScore = Math.min(Math.log(item.getReviewCount() + 1) / Math.log(100), 1.0) * 15;
            baseScore += popularityScore;
        }

        return Math.round(Math.min(baseScore, 100.0) * 10.0) / 10.0;
    }

    private List<String> extractKeywordsFromResult(NlpRecommendationService.RecommendationResult result) {
        // 결과에서 키워드 추출 로직
        return Optional.ofNullable(result.getItems())
                .orElse(Collections.emptyList())
                .stream()
                .flatMap(item -> Arrays.stream(new String[]{
                        item.getCategory().toString(),
                        item.getStyle() != null ? item.getStyle().toString() : "",
                        item.getSeason() != null ? item.getSeason().toString() : ""
                }))
                .filter(s -> !s.isEmpty())
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
    }

    private List<String> generateSearchSuggestions(String query) {
        // 기본 패션 검색 제안
        List<String> suggestions = Arrays.asList(
                "봄 캐주얼 코디",
                "데이트 룩 추천",
                "오피스 정장 스타일",
                "편안한 일상복",
                "트렌디한 아우터",
                "여행용 편안한 옷",
                "파티 드레스",
                "운동복 세트"
        );

        // 쿼리와 관련된 제안 필터링
        return suggestions.stream()
                .filter(suggestion -> !suggestion.toLowerCase().contains(query.toLowerCase()))
                .limit(4)
                .collect(Collectors.toList());
    }

    // === DTO 클래스들 ===

    @lombok.Data
    @lombok.Builder
    public static class RecommendItemDto {
        private Long id;
        private String itemName;
        private Integer price;
        private String imagePath;
        private String category;
        private String style;
        private String recommendationReason;
        private Double itemRating;
        private Double aiScore;
        private boolean semanticMatch; // 시맨틱 매칭 여부
    }

    @lombok.Data
    public static class NLPRecommendRequest {
        private String query;
        private Map<String, Object> userContext;
        private int limit = 10;
    }

    @lombok.Data
    public static class RecommendationFeedback {
        private String query;
        private String intent;
        private int satisfaction; // 1-5
        private List<Long> clickedItems;
        private List<Long> purchasedItems;
        private String userComment;
        private Long userId;
    }
}