package zerobase.MyShoppingMall.service.recommend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.Season;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NlpRecommendationService {

    private final ItemRepository itemRepository;

    @Qualifier("nlpWebClient")
    private final WebClient nlpWebClient;

    @Value("${nlp.service.timeout:30s}")
    private String timeout;

    @Value("${nlp.cache.ttl:3600}")
    private long cacheTtl;

    /**
     * 자연어 기반 상품 추천 (메인 메서드)
     */
    public RecommendationResult recommendByNaturalLanguage(String query, int limit, Map<String, Object> userContext) {
        log.info("자연어 추천 요청: {}", query);
        long startTime = System.currentTimeMillis();

        try {
            // 1. Python NLP 서버에서 쿼리 분석
            QueryAnalysisResult queryAnalysis = analyzeQueryWithNLP(query, userContext);
            log.debug("쿼리 분석 완료: 의도={}, 신뢰도={}", queryAnalysis.getIntent(), queryAnalysis.getConfidence());

            // 2. 후보 상품 필터링 (DB 레벨에서 1차 필터링)
            List<Item> candidateProducts = getCandidateProducts(queryAnalysis);
            log.debug("후보 상품 수: {}", candidateProducts.size());

            if (candidateProducts.isEmpty()) {
                return createFallbackRecommendation(query, limit);
            }

            // 3. Python 서버 : 상품 점수 계산
            List<ProductScore> scoredProducts = scoreProductsWithNLP(queryAnalysis, candidateProducts);

            // 4. 결과 필터링 및 정렬
            List<Item> recommendedItems = scoredProducts.stream()
                    .filter(score -> score.getScore() > 0.3) // 임계값
                    .limit(limit)
                    .map(score -> findItemById(candidateProducts, score.getProductId()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 5. 추천 이유 생성
            Map<Long, String> recommendationReasons = generateRecommendationReasons(queryAnalysis, scoredProducts);

            long endTime = System.currentTimeMillis();
            log.info("자연어 추천 완료: {}개 상품, 소요시간: {}ms", recommendedItems.size(), endTime - startTime);

            return RecommendationResult.builder()
                    .success(true)
                    .query(query)
                    .intent(queryAnalysis.getIntent())
                    .items(recommendedItems)
                    .reasons(recommendationReasons)
                    .confidence(queryAnalysis.getConfidence())
                    .processingTime(endTime - startTime)
                    .totalCandidates(candidateProducts.size())
                    .model("Python-E5-Korean")
                    .build();

        } catch (Exception e) {
            log.error("자연어 추천 실패, 폴백 추천 실행: {}", e.getMessage(), e);
            return createFallbackRecommendation(query, limit);
        }
    }

    /**
     * Python NLP 서버에 쿼리 분석 요청
     */
    @Cacheable(value = "query-analysis", key = "#query.hashCode()")
    public QueryAnalysisResult analyzeQueryWithNLP(String query, Map<String, Object> userContext) {
        try {
            NLPQueryRequest request = NLPQueryRequest.builder()
                    .query(query)
                    .userContext(userContext)
                    .limit(10)
                    .build();

            NLPResponse response = nlpWebClient.post()
                    .uri("/analyze-query")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NLPResponse.class)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                    .timeout(Duration.parse("PT" + timeout))
                    .block();

            if (response != null && response.isSuccess()) {
                return response.getData();
            } else {
                throw new RuntimeException("NLP 서비스 응답 오류");
            }

        } catch (Exception e) {
            log.error("NLP 쿼리 분석 실패: {}", e.getMessage(), e);
            // 폴백: 기본 키워드 분석
            return createFallbackAnalysis(query);
        }
    }

    /**
     * 후보 상품 필터링 (DB 성능 최적화)
     */
    private List<Item> getCandidateProducts(QueryAnalysisResult analysis) {
        List<Item> candidates = new ArrayList<>();

        // 1. 계절 필터링
        if (analysis.getSeason() != null) {
            try {
                Season season = Season.valueOf(analysis.getSeason().toUpperCase());
                candidates.addAll(itemRepository.findBySeasonAndDeleteTypeNot(season, 'Y'));
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 계절 값: {}", analysis.getSeason());
            }
        }

        // 2. 성별 필터링
        if (analysis.getGenderTarget() != null) {
            candidates.addAll(itemRepository.findByGenderAndDeleteTypeNot(analysis.getGenderTarget(), 'Y'));
        }

        // 3. 카테고리 필터링
        if (analysis.getEntities().containsKey("category") && !analysis.getEntities().get("category").isEmpty()) {
            String categoryStr = translateKoreanToEnum(analysis.getEntities().get("category").get(0));
            try {
                ItemCategory category = ItemCategory.valueOf(categoryStr);
                candidates.addAll(itemRepository.findByCategoryAndDeleteTypeNot(category, 'Y'));
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 카테고리 값: {}", categoryStr);
            }
        }

        // 4. 중복 제거
        Set<Long> candidateIds = candidates.stream().map(Item::getId).collect(Collectors.toSet());

        if (candidateIds.isEmpty()) {
            // 필터 조건에 맞는 상품이 없으면 전체 상품에서 선별
            candidates = itemRepository.findAllByDeleteTypeNot('Y');
        } else {
            // 중복 제거
            candidates = candidates.stream()
                    .collect(Collectors.toMap(Item::getId, item -> item, (existing, replacement) -> existing))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
        }

        // 5. 기본 품질 필터링 (평점 2.0 이상)
        return candidates.stream()
                .filter(item -> item.getItemRating() >= 2.0f)
                .limit(200) // 성능을 위해 최대 200개로 제한
                .collect(Collectors.toList());
    }

    /**
     * Python NLP 서버에 상품 점수 계산 요청
     */
    private List<ProductScore> scoreProductsWithNLP(QueryAnalysisResult analysis, List<Item> products) {
        try {
            // 빈 상품 리스트 체크
            if (products.isEmpty()) {
                log.warn("상품 리스트가 비어있습니다.");
                return new ArrayList<>();
            }

            // Item을 Dict 형태로 변환 (안전한 변환)
            List<Map<String, Object>> productDicts = products.stream()
                    .map(this::convertItemToDict)
                    .filter(Objects::nonNull) // null 제거
                    .collect(Collectors.toList());

            if (productDicts.isEmpty()) {
                log.warn("변환된 상품 데이터가 비어있습니다.");
                return new ArrayList<>();
            }

            // 요청 검증
            ProductMatchRequest request = ProductMatchRequest.builder()
                    .queryAnalysis(analysis)
                    .products(productDicts)
                    .build();

            log.debug("Python 서버로 전송할 상품 수: {}", productDicts.size());

            NLPScoreResponse response = nlpWebClient.post()
                    .uri("/score-products")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(NLPScoreResponse.class)
                    .timeout(Duration.parse("PT" + timeout))
                    .block();

            if (response != null && response.isSuccess()) {
                log.debug("Python 서버에서 받은 점수 수: {}", response.getData().size());
                return response.getData();
            } else {
                log.warn("Python 서버 응답이 실패했습니다: {}", response);
                throw new RuntimeException("상품 점수 계산 실패");
            }

        } catch (WebClientResponseException e) {
            log.error("Python 서버 HTTP 오류: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            log.error("요청 데이터 디버그 - 분석결과: {}, 상품수: {}",
                    analysis.getIntent(), products.size());
            // 폴백: 간단한 키워드 매칭 점수
            return createFallbackScores(analysis, products);
        } catch (Exception e) {
            log.error("NLP 상품 점수 계산 실패: {}", e.getMessage(), e);
            // 폴백: 간단한 키워드 매칭 점수
            return createFallbackScores(analysis, products);
        }
    }

    /**
     * 폴백 점수 계산 (키워드 매칭 기반)
     */
    private List<ProductScore> createFallbackScores(QueryAnalysisResult analysis, List<Item> products) {
        return products.stream()
                .map(product -> {
                    double score = calculateKeywordMatchScore(analysis, product);
                    return ProductScore.builder()
                            .productId(product.getId())
                            .score(score)
                            .matchReasons(Arrays.asList("키워드 매칭"))
                            .semanticSimilarity(score)
                            .keywordMatches(extractMatchedKeywords(analysis, product))
                            .build();
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
    }

    /**
     * Item을 Python 서버용 Dict로 변환 (안전한 변환)
     */
    private Map<String, Object> convertItemToDict(Item item) {
        try {
            Map<String, Object> dict = new HashMap<>();

            // 필수 필드
            dict.put("id", item.getId());

            // 선택적 필드들 - null 체크
            dict.put("itemName", item.getItemName() != null ? item.getItemName() : "");
            dict.put("category", item.getCategory() != null ? item.getCategory().toString() : "");
            dict.put("style", item.getStyle() != null ? item.getStyle().toString() : "");
            dict.put("season", item.getSeason() != null ? item.getSeason().toString() : "");
            dict.put("gender", item.getGender() != null ? item.getGender() : "");
            dict.put("price", item.getPrice());
            dict.put("rating", item.getItemRating());
            dict.put("reviewCount", item.getReviewCount() != null ? item.getReviewCount() : 0);
            dict.put("description", item.getItemComment() != null ? item.getItemComment() : "");

            return dict;

        } catch (Exception e) {
            log.error("상품 변환 실패 - ID: {}, 오류: {}", item.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * 추천 이유 생성
     */
    private Map<Long, String> generateRecommendationReasons(QueryAnalysisResult analysis, List<ProductScore> scores) {
        Map<Long, String> reasons = new HashMap<>();

        for (ProductScore score : scores) {
            StringBuilder reasonBuilder = new StringBuilder();

            if (!score.getMatchReasons().isEmpty()) {
                reasonBuilder.append(String.join(", ", score.getMatchReasons()));
            }

            if (!score.getKeywordMatches().isEmpty()) {
                if (reasonBuilder.length() > 0) reasonBuilder.append(" | ");
                reasonBuilder.append("키워드: ").append(String.join(", ", score.getKeywordMatches()));
            }

            if (reasonBuilder.length() == 0) {
                reasonBuilder.append("AI 시맨틱 분석 매칭");
            }

            // 신뢰도 표시
            if (analysis.getConfidence() > 0.8) {
                reasonBuilder.append(" (높은 매칭도)");
            }

            reasons.put(score.getProductId(), reasonBuilder.toString());
        }

        return reasons;
    }

    /**
     * 폴백 추천 생성 (NLP 서버 오류시)
     */
    private RecommendationResult createFallbackRecommendation(String query, int limit) {
        log.info("폴백 추천 실행: {}", query);

        List<Item> fallbackItems = itemRepository.findAllByDeleteTypeNot('Y').stream()
                .filter(item -> {
                    String searchText = (item.getItemName() + " " +
                            item.getCategory() + " " +
                            item.getStyle()).toLowerCase();
                    return Arrays.stream(query.toLowerCase().split("\\s+"))
                            .anyMatch(keyword -> keyword.length() > 1 && searchText.contains(keyword));
                })
                .limit(limit)
                .collect(Collectors.toList());

        Map<Long, String> fallbackReasons = fallbackItems.stream()
                .collect(Collectors.toMap(Item::getId, item -> "키워드 기반 매칭"));

        return RecommendationResult.builder()
                .success(true)
                .query(query)
                .intent("fallback_search")
                .items(fallbackItems)
                .reasons(fallbackReasons)
                .confidence(0.5)
                .processingTime(0L)
                .totalCandidates(fallbackItems.size())
                .model("Fallback-Keyword")
                .build();
    }

    /**
     * 폴백 쿼리 분석
     */
    private QueryAnalysisResult createFallbackAnalysis(String query) {
        return QueryAnalysisResult.builder()
                .intent("general_search")
                .entities(extractBasicEntities(query))
                .keywords(Arrays.asList(query.split("\\s+")))
                .semanticVector(new ArrayList<>())
                .confidence(0.3)
                .processedQuery(query)
                .build();
    }

    /**
     * 기본 엔티티 추출 (폴백용)
     */
    private Map<String, List<String>> extractBasicEntities(String query) {
        Map<String, List<String>> entities = new HashMap<>();
        String lowerQuery = query.toLowerCase();

        // 계절 감지
        if (lowerQuery.contains("봄"))
            entities.put("season", Arrays.asList("SPRING"));
        else if (lowerQuery.contains("여름"))
            entities.put("season", Arrays.asList("SUMMER"));
        else if (lowerQuery.contains("가을"))
            entities.put("season", Arrays.asList("AUTUMN"));
        else if (lowerQuery.contains("겨울"))
            entities.put("season", Arrays.asList("WINTER"));

        // 성별 감지
        if (lowerQuery.contains("남성") || lowerQuery.contains("남자")) {
            entities.put("gender", Arrays.asList("M"));
        } else if (lowerQuery.contains("여성") || lowerQuery.contains("여자")) {
            entities.put("gender", Arrays.asList("F"));
        }

        return entities;
    }

    /**
     * 키워드 매칭 점수 계산 (폴백용)
     */
    private double calculateKeywordMatchScore(QueryAnalysisResult analysis, Item item) {
        double score = 0.0;
        String itemText = (item.getItemName() + " " +
                item.getCategory() + " " +
                item.getStyle()).toLowerCase();

        for (String keyword : analysis.getKeywords()) {
            if (itemText.contains(keyword.toLowerCase())) {
                score += 0.2;
            }
        }

        // 엔티티 매칭 보너스
        if (analysis.getSeason() != null && item.getSeason() != null) {
            if (analysis.getSeason().equals(item.getSeason().toString())) {
                score += 0.3;
            }
        }

        if (analysis.getGenderTarget() != null && item.getGender() != null) {
            if (analysis.getGenderTarget().equals(item.getGender())) {
                score += 0.2;
            }
        }

        return Math.min(score, 1.0);
    }

    /**
     * 매칭된 키워드 추출
     */
    private List<String> extractMatchedKeywords(QueryAnalysisResult analysis, Item item) {
        List<String> matches = new ArrayList<>();
        String itemText = (item.getItemName() + " " +
                item.getCategory() + " " +
                item.getStyle()).toLowerCase();

        for (String keyword : analysis.getKeywords()) {
            if (itemText.contains(keyword.toLowerCase())) {
                matches.add(keyword);
            }
        }

        return matches;
    }

    /**
     * 한국어를 Enum으로 변환
     */
    private String translateKoreanToEnum(String korean) {
        Map<String, String> translations = Map.of(
                "상의", "MENS_TOP",
                "하의", "MENS_BOTTOM",
                "아우터", "MENS_OUTER",
                "원피스", "WOMENS_TOP",
                "신발", "MENS_SHOES",
                "액세서리", "MENS_ACCESSORY"
        );
        return translations.getOrDefault(korean, korean.toUpperCase());
    }

    /**
     * ID로 아이템 찾기
     */
    private Item findItemById(List<Item> items, Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * NLP 서비스 상태 확인
     */
    public boolean isNLPServiceHealthy() {
        try {
            String response = nlpWebClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return response != null && response.contains("healthy");

        } catch (Exception e) {
            log.warn("NLP 서비스 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    // DTO 클래스들
    @lombok.Data
    @lombok.Builder
    public static class RecommendationResult {
        private boolean success;
        private String query;
        private String intent;
        private List<Item> items;
        private Map<Long, String> reasons;
        private double confidence;
        private long processingTime;
        private int totalCandidates;
        private String model;
    }

    @lombok.Data
    @lombok.Builder
    public static class NLPQueryRequest {
        private String query;
        private Map<String, Object> userContext;
        private int limit;
    }

    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryAnalysisResult {

        @JsonProperty("semantic_vector")
        private List<Double> semanticVector;

        @JsonProperty("processed_query")
        private String processedQuery;

        @JsonProperty("gender_target")
        private String genderTarget;

        @JsonProperty("season")
        private String season;

        @JsonProperty("occasion")
        private String occasion;

        @JsonProperty("style_preference")
        private String stylePreference;

        private String intent;
        private Map<String, List<String>> entities;
        private List<String> keywords;
        private double confidence;
    }

    @lombok.Data
    @lombok.Builder
    public static class ProductMatchRequest {
        @JsonProperty("query_analysis")
        private QueryAnalysisResult queryAnalysis;
        private List<Map<String, Object>> products;
    }

    @lombok.Data
    @lombok.Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductScore {
        @JsonProperty("product_id")
        private Long productId;

        @JsonProperty("match_reasons")
        private List<String> matchReasons;

        @JsonProperty("semantic_similarity")
        private double semanticSimilarity;

        @JsonProperty("keyword_matches")
        private List<String> keywordMatches;

        private double score;
    }

    @lombok.Data
    public static class NLPResponse {
        private boolean success;
        private QueryAnalysisResult data;
        private String error;
    }

    @lombok.Data
    public static class NLPScoreResponse {
        private boolean success;
        private List<ProductScore> data;
        private String error;
    }
}