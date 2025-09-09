package zerobase.MyShoppingMall.temps;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    @Qualifier("recommendationRestTemplate")
    private final RestTemplate restTemplate;
    private final ItemService itemService;

    // 🔧 수정: FastAPI 서버 주소로 변경 (8000번 포트)
    @Value("${recommendation.api.url:http://localhost:8000}")
    private String recommendationApiUrl;

    /**
     * 사용자 맞춤 추천
     */
    public RecommendationResponse getRecommendations(Long userId, int numRecommendations, String algorithm) {
        try {
            String url = recommendationApiUrl + "/api/recommendations";

            Map<String, Object> request = new HashMap<>();
            request.put("user_id", userId);
            request.put("num_recommendations", numRecommendations);
            request.put("algorithm", algorithm);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<RecommendationResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, RecommendationResponse.class
            );

            log.info("추천 결과 조회 성공 - 사용자: {}, 결과: {}개", userId,
                    response.getBody() != null ? response.getBody().getRecommendations().size() : 0);

            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("추천 API 호출 실패 - 사용자: {}, 상태: {}, 메시지: {}",
                    userId, e.getStatusCode(), e.getMessage());
            // 🔧 추가: 실패 시 빈 응답 반환 (페이지가 깨지지 않도록)
            return createEmptyRecommendationResponse(userId, algorithm);
        } catch (Exception e) {
            log.error("추천 서비스 오류 - 사용자: {}", userId, e);
            return createEmptyRecommendationResponse(userId, algorithm);
        }
    }

    /**
     * 실시간 추천
     */
    public Map<String, Object> getRealTimeRecommendations(Long userId, List<Integer> recentViews,
                                                          List<Integer> currentCart, int numRecommendations) {
        try {
            String url = recommendationApiUrl + "/api/recommendations/realtime";

            Map<String, Object> request = new HashMap<>();
            request.put("user_id", userId);
            request.put("recent_views", recentViews);
            request.put("current_cart", currentCart);
            request.put("num_recommendations", numRecommendations);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            log.info("실시간 추천 조회 성공 - 사용자: {}", userId);
            return response.getBody();

        } catch (Exception e) {
            log.error("실시간 추천 서비스 오류 - 사용자: {}", userId, e);
            // 🔧 추가: 실패 시 빈 응답 반환
            return Map.of("success", false, "recommendations", Collections.emptyList());
        }
    }

    /**
     * 유사상품 추천
     */
    public Map<String, Object> getSimilarItems(List<Integer> itemIds, int numRecommendations) {
        try {
            String url = recommendationApiUrl + "/api/recommendations/similar";
            Map<String, Object> request = new HashMap<>();
            // 🔧 수정: item_ids로 변경 (FastAPI와 일치)
            request.put("item_ids", itemIds);
            request.put("num_recommendations", numRecommendations);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            log.info("유사 상품 추천 조회 성공 - 기준 상품: {}개", itemIds.size());
            return response.getBody();
        } catch (Exception e) {
            log.error("유사 상품 추천 서비스 오류 - 기준 상품:{}", itemIds, e);
            return Map.of("success", false, "recommendations", Collections.emptyList());
        }
    }

    /**
     * 카테고리 기반추천
     */
    public Map<String, Object> getCategoryRecommendations(String category, int numRecommendations) {
        try {
            String url = recommendationApiUrl + "/api/recommendations/category";

            Map<String, Object> request = new HashMap<>();
            request.put("category", category);
            request.put("num_recommendations", numRecommendations);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, Map.class
            );

            log.info("카테고리 추천 조회 성공 - 카테고리: {}", category);
            return response.getBody();
        } catch (Exception e) {
            log.error("카테고리 추천 서비스 오류 - 카테고리: {}", category, e);
            return Map.of("success", false, "recommendations", Collections.emptyList());
        }
    }

    /**
     * 인기상품 조회
     */
    public Map<String, Object> getPopularItems(int numRecommendations) {
        try {
            // 🔧 수정: GET 메서드로 변경
            String url = recommendationApiUrl + "/api/recommendations/popular?num_recommendations=" + numRecommendations;

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            log.info("인기 상품 조회 성공");
            return response.getBody();
        } catch (Exception e) {
            log.error("인기 상품 조회 서비스 오류", e);
            return Map.of("success", false, "recommendations", Collections.emptyList());
        }
    }

    /**
     * 사용자 상호작용 기록
     */
    public void recordUserInteraction(Long userId, Long itemId, String action) {
        try {
            String url = String.format("%s/api/interactions/record?user_id=%d&item_id=%d&action=%s",
                    recommendationApiUrl, userId, itemId, action);

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.debug("사용자 상호작용 기록 성공 - 사용자: {}, 상품: {}, 액션: {}", userId, itemId, action);
        } catch (Exception e) {
            log.warn("사용자 상호작용 기록 실패 - 사용자: {}, 상품: {}, 액션: {}", userId, itemId, action, e);
        }
    }

    /**
     * 추천 시스템 상태 확인
     */
    public boolean isHealthy() {
        try {
            String url = recommendationApiUrl + "/health";

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class
            );

            Map<String, Object> body = response.getBody();

            if (body != null) {
                // ✅ 수정: status 필드 값 확인 로직 개선
                String status = (String) body.get("status");
                boolean isHealthy = "healthy".equals(status) || "partial".equals(status);

                log.info("추천 시스템 상태 확인: {} (응답: {})",
                        isHealthy ? "정상" : "비정상", body);
                return isHealthy;
            } else {
                log.warn("추천 시스템 헬스체크 응답이 null입니다.");
                return false;
            }

        } catch (Exception e) {
            log.warn("추천 시스템 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 데이터 재처리 요청
     */
    public void refreshRecommendationData() {
        try {
            String url = recommendationApiUrl + "/api/system/refresh";

            HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.info("추천 시스템 데이터 재처리 요청 성공");
        } catch (Exception e) {
            log.error("추천 시스템 데이터 재처리 요청 실패", e);
            throw new RecommendationException("데이터 재처리 요청에 실패했습니다.", e);
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "SonStar-SpringBoot-Client");
        return headers;
    }

    public Map<String, Object> getSearchRelatedRecommendations(String query, int limit) {
        // 0) 방어: 빈 검색어는 즉시 폴백
        if (query == null || query.trim().isEmpty()) {
            Pageable pageable = PageRequest.of(0, limit);
            Page<ItemResponseDto> page = itemService.searchItems("", "popular", pageable);
            return Map.of("recommendations", page.getContent());
        }

        // 1) 외부 추천 엔진 시도
        if (recommendationApiUrl != null && !recommendationApiUrl.isBlank()) {
            try {
                String url = String.format(
                        "%s/api/recommendations/search?query=%s&limit=%d",
                        recommendationApiUrl,
                        URLEncoder.encode(query.trim(), StandardCharsets.UTF_8),
                        limit
                );

                HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
                ResponseEntity<RecommendationResponse> resp =
                        restTemplate.exchange(url, HttpMethod.GET, entity, RecommendationResponse.class);

                if (resp.getStatusCode().is2xxSuccessful()
                        && resp.getBody() != null
                        && resp.getBody().getRecommendations() != null) {
                    return Map.of("recommendations", resp.getBody().getRecommendations());
                }
            } catch (Exception ex) {
                log.warn("외부 추천 호출 실패(검색 연관) - 폴백 사용: {}", ex.getMessage());
            }
        }

        // 2) 폴백: 검색 상위 N개(인기순 권장)
        Pageable pageable = PageRequest.of(0, limit);
        Page<ItemResponseDto> page = itemService.searchItems(query.trim(), "popular", pageable);
        return Map.of("recommendations", page.getContent());
    }

    // 🔧 추가: 빈 추천 응답 생성 메서드
    private RecommendationResponse createEmptyRecommendationResponse(Long userId, String algorithm) {
        RecommendationResponse response = new RecommendationResponse();
        response.setSuccess(false);
        response.setMessage("추천 시스템 연결 실패");
        response.setUserId(userId);
        response.setAlgorithm(algorithm);
        response.setRecommendations(Collections.emptyList());
        response.setTimestamp(java.time.LocalDateTime.now().toString());
        return response;
    }

    // 🔧 추가: 안전한 추천 조회 메서드
    public RecommendationResponse getSafeRecommendations(Long userId, int numRecommendations, String algorithm) {
        try {
            // 추천 시스템 상태 확인 (간단히)
            if (!isHealthy()) {
                log.warn("추천 시스템이 비활성 상태입니다. 인기 상품으로 대체합니다.");
                return convertPopularItemsToRecommendationResponse(userId, numRecommendations, algorithm);
            }

            return getRecommendations(userId, numRecommendations, algorithm);
        } catch (Exception e) {
            log.warn("추천 조회 실패, 인기 상품으로 대체 - 사용자: {}", userId, e);
            return convertPopularItemsToRecommendationResponse(userId, numRecommendations, algorithm);
        }
    }

    // 🔧 추가: 인기 상품을 추천 응답 형태로 변환
    private RecommendationResponse convertPopularItemsToRecommendationResponse(Long userId, int numRecommendations, String algorithm) {
        try {
            Pageable pageable = PageRequest.of(0, numRecommendations);
            Page<ItemResponseDto> popularItems = itemService.findItems(null, "popular", null, 0, numRecommendations);

            RecommendationResponse response = new RecommendationResponse();
            response.setSuccess(true);
            response.setMessage("인기 상품 기반 추천");
            response.setUserId(userId);
            response.setAlgorithm("fallback_popular");
            response.setRecommendations(Collections.singletonList(popularItems.getContent()));
            response.setTimestamp(java.time.LocalDateTime.now().toString());

            return response;
        } catch (Exception e) {
            log.error("인기 상품 조회도 실패", e);
            return createEmptyRecommendationResponse(userId, algorithm);
        }
    }
}