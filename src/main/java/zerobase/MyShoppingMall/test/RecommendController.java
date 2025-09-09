//package zerobase.MyShoppingMall.test;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/recommend")
//@RequiredArgsConstructor
//@Slf4j
//public class RecommendController {
//
//    private final NlpRecommendationService nlpRecommendationService;
//
//    @GetMapping("/nlp")
//    public String getNlpRecommendations(@RequestParam String query, Model model) {
//        try {
//            log.info("AI 추천 요청: {}", query);
//
//            // 자연어 처리 및 추천 생성
//            RecommendationResult result = nlpRecommendationService.processNaturalLanguageQuery(query);
//
//            // 모델에 결과 추가
//            model.addAttribute("result", result);
//            model.addAttribute("query", query);
//            model.addAttribute("items", result.getRecommendedItems());
//            model.addAttribute("explanation", result.getExplanation());
//            model.addAttribute("confidence", result.getConfidence());
//            model.addAttribute("entities", result.getExtractedEntities());
//
//            log.info("AI 추천 결과: {}개 상품, 신뢰도: {}",
//                    result.getRecommendedItems().size(), result.getConfidence());
//
//            return "fragments/ai-recommendations";
//
//        } catch (Exception e) {
//            log.error("AI 추천 처리 중 오류 발생", e);
//
//            model.addAttribute("error", true);
//            model.addAttribute("errorMessage", "추천 서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요.");
//
//            return "fragments/ai-recommendations";
//        }
//    }
//
//    @PostMapping("/feedback")
//    @ResponseBody
//    public String submitFeedback(@RequestParam String query,
//                                 @RequestParam Long itemId,
//                                 @RequestParam String feedback) {
//        try {
//            // 피드백 저장 로직 (ML 모델 개선용)
//            log.info("AI 추천 피드백: query={}, itemId={}, feedback={}", query, itemId, feedback);
//            return "success";
//        } catch (Exception e) {
//            log.error("피드백 저장 실패", e);
//            return "error";
//        }
//    }
//}
