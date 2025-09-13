package zerobase.MyShoppingMall.temps.nlp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import zerobase.MyShoppingMall.entity.Item;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/recommend")
@Slf4j
public class RecommendController {
    private final NlpRecommendationService nlpRecommendationService;

    /**
     * NLP 기반 상품 추천 (HTML 프래그먼트 반환)
     */
    @GetMapping("/nlp")
    public String nlpRecommend(@RequestParam("query") String query, Model model) {
        log.info("NLP 추천 요청: {}", query);

        try {
            // 1. 입력 검증
            if (query == null || query.trim().isEmpty()) {
                model.addAttribute("error", "검색어를 입력해주세요.");
                return "fragments/ai-recommend-error";
            }

            // 2. 추천 실행
            List<Item> recommendedItems = nlpRecommendationService.recommendByNaturalLanguage(query, 12);

            if (recommendedItems.isEmpty()) {
                model.addAttribute("query", query);
                model.addAttribute("message", "'" + query + "'에 대한 추천 상품을 찾을 수 없습니다.");
                return "fragments/ai-recommend-empty";
            }

            // 3. 추천 이유 생성
            List<RecommendItemDto> recommendDtos = recommendedItems.stream()
                    .map(item -> {
                        String reason = nlpRecommendationService.generateRecommendationReason(item, query);
                        return RecommendItemDto.builder()
                                .id(item.getId())
                                .itemName(item.getItemName())
                                .price(item.getPrice())
                                .imagePath(item.getImageUrl())
                                .category(String.valueOf(item.getCategory()))
                                .style(String.valueOf(item.getStyle()))
                                .recommendationReason(reason)
                                .itemRating((double) item.getItemRating())
                                .build();
                    })
                    .collect(Collectors.toList());

            // 4. 모델에 데이터 추가
            model.addAttribute("query", query);
            model.addAttribute("recommendedItems", recommendDtos);
            model.addAttribute("totalCount", recommendDtos.size());
            model.addAttribute("keywords", extractDisplayKeywords(query));

            return "fragments/ai-recommend-result";

        } catch (Exception e) {
            log.error("NLP 추천 중 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("error", "추천 시스템에 일시적인 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return "fragments/ai-recommend-error";
        }
    }

    /**
     * API 버전 - JSON 응답
     */
    @GetMapping("/api/nlp")
    @ResponseBody
    public ResponseEntity<?> nlpRecommendApi(@RequestParam("query") String query,
                                             @RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            List<Item> recommendedItems = nlpRecommendationService.recommendByNaturalLanguage(query, limit);

            List<Map<String, Object>> result = recommendedItems.stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("id", item.getId());
                        itemMap.put("itemName", item.getItemName());
                        itemMap.put("price", item.getPrice());
                        itemMap.put("imagePath", item.getImageUrl());
                        itemMap.put("category", item.getCategory());
                        itemMap.put("reason", nlpRecommendationService.generateRecommendationReason(item, query));
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "query", query,
                    "count", result.size(),
                    "items", result
            ));

        } catch (Exception e) {
            log.error("API NLP 추천 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", "추천 시스템 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 추천 시스템 헬스체크
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("recommendation_service", true);
        health.put("nlp_service", true);
        health.put("timestamp", new Date());

        return ResponseEntity.ok(health);
    }

    /**
     * 화면 표시용 키워드 추출
     */
    private List<String> extractDisplayKeywords(String query) {
        // 간단한 키워드 추출 (실제로는 NLP 서비스의 키워드 추출 로직 활용)
        Set<String> keywords = new HashSet<>();

        // 패션 관련 주요 키워드들
        String[] fashionKeywords = {
                "캐주얼", "포멀", "스포티", "빈티지", "미니멀", "엘레간트",
                "봄", "여름", "가을", "겨울", "시원", "따뜻", "산뜻",
                "데이트", "출근", "여행", "파티", "일상",
                "상의", "하의", "아우터", "원피스", "신발", "액세서리",
                "블랙", "화이트", "네이비", "베이지", "세련", "귀여운"
        };

        String lowerQuery = query.toLowerCase();
        for (String keyword : fashionKeywords) {
            if (lowerQuery.contains(keyword.toLowerCase())) {
                keywords.add(keyword);
            }
        }

        return new ArrayList<>(keywords);
    }

    /**
     * 추천 상품 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class RecommendItemDto {
        private Long id;
        private String itemName;
        private Integer price;
        private String imagePath;
        private String category;
        private String style;
        private String recommendationReason;
        private Double itemRating;
    }
}