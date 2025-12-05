//package zerobase.MyShoppingMall.temps;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@Slf4j
//@RequestMapping("/api/recommendations")
//@RequiredArgsConstructor
//public class RecommendationController {
//
//    private final RecommendationService recommendationService;
//
//    /**
//     * 사용자 맞춤 추천 기능
//     */
//    @PostMapping("/user/{userId}")
//    public ResponseEntity<RecommendationResponse> getUserRecommendation(
//            @PathVariable Long userId,
//            @RequestParam(defaultValue = "10") int count,
//            @RequestParam(defaultValue = "hybrid") String algorithm) {
//
//        log.info("사용자 추천 요청 - 사용자:{}, 개수: {}, 알고리즘: {}", userId, count, algorithm);
//
//        RecommendationResponse recommendations = recommendationService.getRecommendations(userId, count, algorithm);
//        return ResponseEntity.ok(recommendations);
//    }
//
//    /**
//     * 실시간 추천
//     */
//    @PostMapping("/realtime")
//    public ResponseEntity<Map<String, Object>> getRealtimeRecommendations(
//            @RequestParam Long userId,
//            @RequestParam(required = false) List<Integer> recentViews,
//            @RequestParam(required = false) List<Integer> currentCart,
//            @RequestParam(defaultValue = "5") int count) {
//
//        log.info("실시간 추천 요청 - 사용자: {}, 개수: {}", userId, count);
//
//        Map<String, Object> recommendations = recommendationService.getRealTimeRecommendations(
//                userId, recentViews, currentCart, count);
//        return ResponseEntity.ok(recommendations);
//    }
//
//    /**
//     * 유사 상품 추천
//     */
//    @PostMapping("/similar")
//    public ResponseEntity<Map<String, Object>> getSimilarItems(
//            @RequestParam List<Integer> itemIds,
//            @RequestParam(defaultValue = "5") int count) {
//
//        log.info("유사 상품 추천 - 기준 상품: {}, 개수: {}", itemIds, count);
//        Map<String, Object> recommendations = recommendationService.getSimilarItems(itemIds, count);
//        return ResponseEntity.ok(recommendations);
//    }
//
//    /**
//     * 카테고리 기반 추천
//     */
//    @GetMapping("/category/{category}")
//    public ResponseEntity<Map<String, Object>> getCategoryRecommendations(
//            @PathVariable String category,
//            @RequestParam(defaultValue = "10") int count) {
//
//        log.info("카테고리 추천 요청 - 카테고리: {}, 개수: {}", category, count);
//
//        Map<String, Object> recommendations = recommendationService.getCategoryRecommendations(category, count);
//        return ResponseEntity.ok(recommendations);
//    }
//
//    /**
//     * 인기 상품 추천
//     */
//    @GetMapping("/popular")
//    public ResponseEntity<Map<String, Object>> getPopularItems(
//            @RequestParam(defaultValue = "10") int count) {
//
//        log.info("인기 상품 조회 요청 - 개수: {}", count);
//
//        Map<String, Object> popularItems = recommendationService.getPopularItems(count);
//        return ResponseEntity.ok(popularItems);
//    }
//
//    /**
//     * 사용자 상호작용 기록
//     */
//    @PostMapping("/interactions")
//    public ResponseEntity<String> recordInteraction(
//            @RequestParam Long userId,
//            @RequestParam Long itemId,
//            @RequestParam String action) {
//
//        log.debug("상호작용 기록 요청 - 사용자: {}, 상품: {}, 액션: {}", userId, itemId, action);
//
//        recommendationService.recordUserInteraction(userId, itemId, action);
//        return ResponseEntity.ok("상호작용이 기록되었습니다.");
//    }
//
//    /**
//     * 시스템 상태 확인
//     */
//    @GetMapping("/health")
//    public ResponseEntity<Map<String, Object>> getSystemHealth() {
//        boolean isHealthy = recommendationService.isHealthy();
//
//        return ResponseEntity.ok(Map.of(
//                "status", isHealthy ? "healthy" : "unhealthy",
//                "recommendation_service", isHealthy
//        ));
//    }
//
//    /**
//     * 데이터 재처리 요청 (관리자용)
//     */
//    @PostMapping("/admin/refresh")
//    public ResponseEntity<String> refreshData() {
//        log.info("추천 시스템 데이터 재처리 요청");
//
//        recommendationService.refreshRecommendationData();
//        return ResponseEntity.ok("데이터 재처리 요청이 완료되었습니다.");
//    }
//
//}