package zerobase.MyShoppingMall.temps.nlp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.repository.item.ItemRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NlpRecommendationService {

    private final ItemRepository itemRepository;

    // 패션 관련 키워드 매핑 테이블
    private static final Map<String, List<String>> FASHION_KEYWORDS = Map.ofEntries(
            // 스타일 키워드
            Map.entry("캐주얼", Arrays.asList("캐주얼", "편안", "데일리", "일상", "자연스러운")),
            Map.entry("포멀", Arrays.asList("포멀", "정장", "비즈니스", "회사", "공식", "격식")),
            Map.entry("스포티", Arrays.asList("스포티", "운동", "스포츠", "액티브", "활동적")),
            Map.entry("빈티지", Arrays.asList("빈티지", "레트로", "클래식", "고전", "옛날")),
            Map.entry("미니멀", Arrays.asList("미니멀", "심플", "단순", "깔끔", "간결")),

            // 계절 키워드
            Map.entry("봄", Arrays.asList("봄", "따뜻", "산뜻", "상큼", "밝은", "화사", "생기")),
            Map.entry("여름", Arrays.asList("여름", "시원", "가벼운", "시원한", "청량", "상쾌")),
            Map.entry("가을", Arrays.asList("가을", "가을", "따뜻한", "포근", "차분", "우아")),
            Map.entry("겨울", Arrays.asList("겨울", "따뜻한", "포근", "아늑", "두꺼운", "보온")),

            // 상황 키워드
            Map.entry("데이트", Arrays.asList("데이트", "로맨틱", "예쁜", "매력적", "우아", "섹시")),
            Map.entry("출근", Arrays.asList("출근", "오피스", "업무", "직장", "정중", "단정")),
            Map.entry("여행", Arrays.asList("여행", "편안", "실용적", "활동적", "자유로운")),
            Map.entry("파티", Arrays.asList("파티", "화려", "드레시", "특별", "멋진", "세련")),

            // 색상 키워드
            Map.entry("블랙", Arrays.asList("검은", "블랙", "어두운", "시크", "모던")),
            Map.entry("화이트", Arrays.asList("흰", "화이트", "밝은", "깔끔", "순수")),
            Map.entry("네이비", Arrays.asList("네이비", "남색", "진한", "차분", "안정적")),
            Map.entry("베이지", Arrays.asList("베이지", "연한", "자연스러운", "중성적")),

            // 분위기 키워드
            Map.entry("세련", Arrays.asList("세련", "시크", "모던", "트렌디", "스타일리시")),
            Map.entry("귀여운", Arrays.asList("귀여운", "큐트", "사랑스러운", "달콤", "러블리")),
            Map.entry("엘레간트", Arrays.asList("엘레간트", "우아", "고급", "품격", "클래식"))
    );

    // 카테고리 매핑
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
            "상의", Arrays.asList("상의", "티셔츠", "셔츠", "블라우스", "탑", "니트", "스웨터"),
            "하의", Arrays.asList("하의", "바지", "팬츠", "청바지", "스커트", "레깅스"),
            "아우터", Arrays.asList("아우터", "자켓", "코트", "점퍼", "가디건", "조끼"),
            "원피스", Arrays.asList("원피스", "드레스", "롱드레스", "미니드레스"),
            "신발", Arrays.asList("신발", "구두", "스니커즈", "부츠", "샌들", "슬리퍼"),
            "액세서리", Arrays.asList("액세서리", "가방", "모자", "벨트", "목걸이", "귀걸이", "시계")
    );

    /**
     * 자연어 입력을 분석하여 상품 추천
     */
    public List<Item> recommendByNaturalLanguage(String query, int limit) {
        log.info("NLP 추천 요청: {}", query);

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 키워드 추출
        Set<String> extractedKeywords = extractKeywords(query.toLowerCase());
        log.info("추출된 키워드: {}", extractedKeywords);

        // 2. 모든 상품 조회
        List<Item> allItems = itemRepository.findAllByDeleteTypeNot('Y');

        // 3. 상품별 점수 계산
        List<ScoredItem> scoredItems = allItems.stream()
                .map(item -> new ScoredItem(item, calculateScore(item, extractedKeywords)))
                .filter(scoredItem -> scoredItem.score > 0) // 점수가 0보다 큰 것만
                .sorted((a, b) -> Double.compare(b.score, a.score)) // 점수 내림차순
                .limit(limit)
                .collect(Collectors.toList());

        log.info("추천 결과 개수: {}", scoredItems.size());

        return scoredItems.stream()
                .map(scoredItem -> scoredItem.item)
                .collect(Collectors.toList());
    }

    /**
     * 입력 텍스트에서 키워드 추출
     */
    private Set<String> extractKeywords(String query) {
        Set<String> keywords = new HashSet<>();

        // 1. 직접 매칭되는 키워드 찾기
        for (Map.Entry<String, List<String>> entry : FASHION_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (query.contains(keyword)) {
                    keywords.add(entry.getKey());
                    keywords.addAll(entry.getValue());
                    break;
                }
            }
        }

        // 2. 카테고리 키워드 찾기
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (query.contains(keyword)) {
                    keywords.add(entry.getKey());
                    keywords.addAll(entry.getValue());
                    break;
                }
            }
        }

        // 3. 원본 단어들도 추가 (공백으로 분리)
        String[] words = query.split("\\s+");
        for (String word : words) {
            word = word.trim().replaceAll("[^가-힣a-zA-Z]", ""); // 특수문자 제거
            if (word.length() > 1) {
                keywords.add(word);
            }
        }

        return keywords;
    }

    /**
     * 상품과 키워드의 유사도 점수 계산
     */
    private double calculateScore(Item item, Set<String> keywords) {
        double score = 0.0;

        // 1. 상품명 매칭 (가중치: 3)
        String itemName = item.getItemName().toLowerCase();
        for (String keyword : keywords) {
            if (itemName.contains(keyword)) {
                score += 3.0;
            }
        }

        // 2. 상품 설명 매칭 (가중치: 2)
        if (item.getItemComment() != null) {
            String description = item.getItemComment().toLowerCase();
            for (String keyword : keywords) {
                if (description.contains(keyword)) {
                    score += 2.0;
                }
            }
        }

        // 3. 카테고리 매칭 (가중치: 4)
        if (item.getCategory() != null) {
            String category = item.getCategory().toLowerCase();
            for (String keyword : keywords) {
                if (category.contains(keyword) || keyword.contains(category)) {
                    score += 4.0;
                }
            }
        }

        // 4. 스타일 매칭 (가중치: 3)
        if (item.getStyle() != null) {
            String style = item.getStyle().toLowerCase();
            for (String keyword : keywords) {
                if (style.contains(keyword) || keyword.contains(style)) {
                    score += 3.0;
                }
            }
        }

        // 5. 성별 매칭 (가중치: 2)
        if (item.getGender() != null) {
            String gender = item.getGender().toLowerCase();
            for (String keyword : keywords) {
                if (gender.contains(keyword) || keyword.contains(gender)) {
                    score += 2.0;
                }
            }
        }

        // 6. 시즌 매칭 (가중치: 3)
        if (item.getSeason() != null) {
            String season = item.getSeason().toLowerCase();
            for (String keyword : keywords) {
                if (season.contains(keyword) || keyword.contains(season)) {
                    score += 3.0;
                }
            }
        }

        // 7. 인기도 보정 (평점과 리뷰 수 고려)
        Integer reviewCount = item.getReviewCount();
            Double itemRating = (double) item.getItemRating();

        if (reviewCount != null && reviewCount > 0 && itemRating != null) {
            double popularityBonus = (itemRating * Math.log(reviewCount + 1)) * 0.1;
            score += popularityBonus;
        }

        return score;
    }

    /**
     * 점수가 포함된 상품 클래스
     */
    private static class ScoredItem {
        final Item item;
        final double score;

        ScoredItem(Item item, double score) {
            this.item = item;
            this.score = score;
        }
    }

    /**
     * 추천 이유 생성
     */
    public String generateRecommendationReason(Item item, String originalQuery) {
        Set<String> keywords = extractKeywords(originalQuery.toLowerCase());
        List<String> matchedReasons = new ArrayList<>();

        // 매칭된 이유들 수집
        String itemName = item.getItemName().toLowerCase();
        for (String keyword : keywords) {
            if (itemName.contains(keyword)) {
                matchedReasons.add(keyword);
            }
        }

        if (item.getCategory() != null) {
            String category = item.getCategory().toLowerCase();
            for (String keyword : keywords) {
                if (category.contains(keyword) || keyword.contains(category)) {
                    matchedReasons.add(category + " 카테고리");
                    break;
                }
            }
        }

        if (item.getStyle() != null) {
            String style = item.getStyle().toLowerCase();
            for (String keyword : keywords) {
                if (style.contains(keyword) || keyword.contains(style)) {
                    matchedReasons.add(style + " 스타일");
                    break;
                }
            }
        }

        if (matchedReasons.isEmpty()) {
            return "AI 추천 상품";
        }

        return matchedReasons.stream().limit(2).collect(Collectors.joining(", ")) + " 추천";
    }
}