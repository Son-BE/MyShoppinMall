//package zerobase.MyShoppingMall.test;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import zerobase.MyShoppingMall.dto.item.ItemDto;
//import zerobase.MyShoppingMall.entity.Item;
//import zerobase.MyShoppingMall.repository.item.ItemRepository;
//
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class NlpRecommendationService {
//
//    private final ItemRepository itemRepository;
//
//    // 스타일 키워드 매핑
//    private static final Map<String, List<String>> STYLE_KEYWORDS = Map.of(
//            "캐주얼", Arrays.asList("캐주얼", "편안", "일상", "데일리", "심플"),
//            "포멀", Arrays.asList("포멀", "정장", "비즈니스", "오피스", "클래식"),
//            "스트릿", Arrays.asList("스트릿", "힙합", "트렌디", "유니크", "개성"),
//            "미니멀", Arrays.asList("미니멀", "깔끔", "심플", "베이직", "모던"),
//            "빈티지", Arrays.asList("빈티지", "레트로", "클래식", "앤틱", "올드")
//    );
//
//    // 상황 키워드 매핑
//    private static final Map<String, List<String>> OCCASION_KEYWORDS = Map.of(
//            "데이트", Arrays.asList("데이트", "연인", "커플", "로맨틱"),
//            "출근", Arrays.asList("출근", "회사", "업무", "오피스", "직장"),
//            "파티", Arrays.asList("파티", "모임", "파티복", "이벤트"),
//            "여행", Arrays.asList("여행", "휴가", "바캉스", "관광"),
//            "운동", Arrays.asList("운동", "헬스", "조깅", "스포츠", "트레이닝")
//    );
//
//    // 카테고리 키워드 매핑
//    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.of(
//            "상의", Arrays.asList("상의", "티셔츠", "셔츠", "맨투맨", "후드", "탑"),
//            "하의", Arrays.asList("하의", "바지", "팬츠", "청바지", "슬랙스", "조거팬츠"),
//            "아우터", Arrays.asList("아우터", "자켓", "코트", "패딩", "바람막이", "가디건"),
//            "원피스", Arrays.asList("원피스", "드레스", "투피스"),
//            "신발", Arrays.asList("신발", "스니커즈", "운동화", "구두", "부츠", "샌들"),
//            "액세서리", Arrays.asList("액세서리", "가방", "시계", "모자", "반지", "목걸이")
//    );
//
//    public RecommendationResult processNaturalLanguageQuery(String query) {
//        try {
//            // 1. 쿼리 전처리
//            String cleanQuery = preprocessQuery(query);
//
//            // 2. 의도 분석
//            String intent = classifyIntent(cleanQuery);
//
//            // 3. 엔티티 추출
//            Map<String, String> entities = extractEntities(cleanQuery);
//
//            // 4. 상품 검색 및 랭킹
//            List<ItemDto> recommendedItems = findAndRankItems(entities, cleanQuery);
//
//            // 5. 결과 설명 생성
//            String explanation = generateExplanation(intent, entities, recommendedItems.size());
//
//            // 6. 신뢰도 계산
//            double confidence = calculateConfidence(entities, recommendedItems);
//
//            return new RecommendationResult(
//                    query, intent, entities, recommendedItems,
//                    explanation, confidence
//            );
//
//        } catch (Exception e) {
//            // 오류 시 기본 추천 제공
//            return createFallbackRecommendation(query);
//        }
//    }
//
//    private String preprocessQuery(String query) {
//        return query.toLowerCase()
//                .trim()
//                .replaceAll("[^가-힣a-z0-9\\s]", " ")
//                .replaceAll("\\s+", " ");
//    }
//
//    private String classifyIntent(String query) {
//        if (query.contains("추천") || query.contains("골라") || query.contains("찾아")) {
//            return "RECOMMENDATION";
//        } else if (query.contains("코디") || query.contains("매치") || query.contains("조합")) {
//            return "COORDINATION";
//        } else if (query.contains("트렌드") || query.contains("유행") || query.contains("인기")) {
//            return "TREND";
//        } else {
//            return "SEARCH";
//        }
//    }
//
//    private Map<String, String> extractEntities(String query) {
//        Map<String, String> entities = new HashMap<>();
//
//        // 성별 추출
//        if (query.contains("남성") || query.contains("남자") || query.contains("멘즈")) {
//            entities.put("gender", "MALE");
//        } else if (query.contains("여성") || query.contains("여자") || query.contains("우먼")) {
//            entities.put("gender", "FEMALE");
//        }
//
//        // 스타일 추출
//        for (Map.Entry<String, List<String>> entry : STYLE_KEYWORDS.entrySet()) {
//            for (String keyword : entry.getValue()) {
//                if (query.contains(keyword)) {
//                    entities.put("style", entry.getKey());
//                    break;
//                }
//            }
//            if (entities.containsKey("style")) break;
//        }
//
//        // 상황 추출
//        for (Map.Entry<String, List<String>> entry : OCCASION_KEYWORDS.entrySet()) {
//            for (String keyword : entry.getValue()) {
//                if (query.contains(keyword)) {
//                    entities.put("occasion", entry.getKey());
//                    break;
//                }
//            }
//            if (entities.containsKey("occasion")) break;
//        }
//
//        // 카테고리 추출
//        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
//            for (String keyword : entry.getValue()) {
//                if (query.contains(keyword)) {
//                    entities.put("category", entry.getKey());
//                    break;
//                }
//            }
//            if (entities.containsKey("category")) break;
//        }
//
//        // 가격대 추출
//        extractPriceRange(query, entities);
//
//        // 연령대 추출
//        extractAgeGroup(query, entities);
//
//        return entities;
//    }
//
//    private void extractPriceRange(String query, Map<String, String> entities) {
//        // "10만원 이하", "5-10만원", "저렴한", "고급" 등
//        if (query.contains("저렴") || query.contains("싸") || query.contains("가성비")) {
//            entities.put("priceRange", "LOW");
//        } else if (query.contains("고급") || query.contains("명품") || query.contains("비싼")) {
//            entities.put("priceRange", "HIGH");
//        } else if (query.contains("적당") || query.contains("중간")) {
//            entities.put("priceRange", "MEDIUM");
//        }
//
//        // 구체적인 금액 추출
//        Pattern pricePattern = Pattern.compile("(\\d+)만원");
//        java.util.regex.Matcher matcher = pricePattern.matcher(query);
//        if (matcher.find()) {
//            int price = Integer.parseInt(matcher.group(1)) * 10000;
//            entities.put("specificPrice", String.valueOf(price));
//        }
//    }
//
//    private void extractAgeGroup(String query, Map<String, String> entities) {
//        if (query.contains("10대") || query.contains("teen")) {
//            entities.put("ageGroup", "TEEN");
//        } else if (query.contains("20대") || query.contains("young")) {
//            entities.put("ageGroup", "TWENTIES");
//        } else if (query.contains("30대") || query.contains("thirty")) {
//            entities.put("ageGroup", "THIRTIES");
//        } else if (query.contains("40대") || query.contains("middle")) {
//            entities.put("ageGroup", "FORTIES");
//        }
//    }
//
//    private List<ItemDto> findAndRankItems(Map<String, String> entities, String originalQuery) {
//        // 1. 기본 필터링
//        List<Item> filteredItems = itemRepository.findAll().stream()
//                .filter(item -> matchesGender(item, entities))
//                .filter(item -> matchesCategory(item, entities))
//                .filter(item -> matchesPriceRange(item, entities))
//                .filter(item -> item.getQuantity() > 0)
//                .collect(Collectors.toList());
//
//        // 2. 점수 계산 및 랭킹
//        List<ItemDto> rankedItems = filteredItems.stream()
//                .map(item -> {
//                    ItemDto dto = convertToDto(item);
//                    double score = calculateMatchScore(item, entities, originalQuery);
//                    dto.setMatchScore(score);
//                    dto.setMatchReason(generateMatchReason(item, entities));
//                    return dto;
//                })
//                .sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
//                .limit(12) // 최대 12개 추천
//                .collect(Collectors.toList());
//
//        return rankedItems;
//    }
//
//    private boolean matchesGender(Item item, Map<String, String> entities) {
//        String requestedGender = entities.get("gender");
//        if (requestedGender == null) return true;
//
//        return item.getGender() == null ||
//                item.getGender().equals("UNISEX") ||
//                item.getGender().equals(requestedGender);
//    }
//
//    private boolean matchesCategory(Item item, Map<String, String> entities) {
//        String requestedCategory = entities.get("category");
//        if (requestedCategory == null) return true;
//
//        return item.getCategory() != null &&
//                item.getCategory().name().equalsIgnoreCase(requestedCategory);
//    }
//
//    private boolean matchesPriceRange(Item item, Map<String, String> entities) {
//        String priceRange = entities.get("priceRange");
//        if (priceRange == null) return true;
//
//        int price = item.getPrice();
//        switch (priceRange) {
//            case "LOW": return price <= 50000;
//            case "MEDIUM": return price > 50000 && price <= 150000;
//            case "HIGH": return price > 150000;
//            default: return true;
//        }
//    }
//
//    private double calculateMatchScore(Item item, Map<String, String> entities, String query) {
//        double score = 0.0;
//
//        // 카테고리 매치 (가중치: 30%)
//        if (entities.containsKey("category") &&
//                item.getCategory() != null &&
//                item.getCategory().name().equalsIgnoreCase(entities.get("category"))) {
//            score += 0.3;
//        }
//
//        // 스타일 매치 (가중치: 25%)
//        if (entities.containsKey("style") &&
//                item.getItemComment() != null &&
//                item.getItemComment().toLowerCase().contains(entities.get("style"))) {
//            score += 0.25;
//        }
//
//        // 가격 매치 (가중치: 20%)
//        if (matchesPriceRange(item, entities)) {
//            score += 0.2;
//        }
//
//        // 키워드 매치 (가중치: 15%)
//        score += calculateKeywordMatch(item, query) * 0.15;
//
//        // 인기도 보너스 (가중치: 10%)
//        // 실제로는 판매량, 조회수 등을 기준으로 계산
//        score += Math.random() * 0.1; // 임시로 랜덤값 사용
//
//        return Math.min(score, 1.0);
//    }
//
//    private double calculateKeywordMatch(Item item, String query) {
//        String itemText = (item.getItemName() + " " +
//                (item.getItemComment() != null ? item.getItemComment() : "")).toLowerCase();
//        String[] queryWords = query.split("\\s+");
//
//        long matchCount = Arrays.stream(queryWords)
//                .filter(word -> word.length() > 1)
//                .filter(itemText::contains)
//                .count();
//
//        return queryWords.length > 0 ? (double) matchCount / queryWords.length : 0.0;
//    }
//
//    private String generateMatchReason(Item item, Map<String, String> entities) {
//        List<String> reasons = new ArrayList<>();
//
//        if (entities.containsKey("style")) {
//            reasons.add(entities.get("style") + " 스타일");
//        }
//        if (entities.containsKey("occasion")) {
//            reasons.add(entities.get("occasion") + "에 적합");
//        }
//        if (entities.containsKey("category")) {
//            reasons.add(entities.get("category") + " 카테고리");
//        }
//        if (entities.containsKey("priceRange")) {
//            reasons.add("가격대 매치");
//        }
//
//        return reasons.isEmpty() ? "일반 추천" : String.join(", ", reasons);
//    }
//
//    private ItemDto convertToDto(Item item) {
//        ItemDto dto = new ItemDto();
//        dto.setId(item.getId());
//        dto.setItemName(item.getItemName());
//        dto.setPrice(item.getPrice());
//        dto.setImagePath(item.getImagePath());
//        dto.setCategory(item.getCategory());
//        dto.setSubCategory(item.getSubCategory());
//        dto.setGender(item.getGender());
//        dto.setDescription(item.getItemComment());
//        dto.setQuantity(item.getQuantity());
//        return dto;
//    }
//
//    private String generateExplanation(String intent, Map<String, String> entities, int itemCount) {
//        StringBuilder explanation = new StringBuilder();
//
//        explanation.append("요청하신 ");
//
//        if (entities.containsKey("gender")) {
//            explanation.append(entities.get("gender").equals("MALE") ? "남성용 " : "여성용 ");
//        }
//
//        if (entities.containsKey("style")) {
//            explanation.append(entities.get("style")).append(" ");
//        }
//
//        if (entities.containsKey("category")) {
//            explanation.append(entities.get("category"));
//        } else {
//            explanation.append("패션 아이템");
//        }
//
//        if (entities.containsKey("occasion")) {
//            explanation.append(" (").append(entities.get("occasion")).append("용)");
//        }
//
//        explanation.append("에 대해 ").append(itemCount).append("개의 추천 상품을 찾았습니다.");
//
//        return explanation.toString();
//    }
//
//    private double calculateConfidence(Map<String, String> entities, List<ItemDto> items) {
//        double confidence = 0.5; // 기본 신뢰도
//
//        // 엔티티가 많을수록 신뢰도 증가
//        confidence += entities.size() * 0.1;
//
//        // 결과가 많을수록 신뢰도 증가 (단, 상한선 적용)
//        confidence += Math.min(items.size() * 0.02, 0.3);
//
//        // 평균 매치 스코어가 높을수록 신뢰도 증가
//        double avgScore = items.stream()
//                .mapToDouble(ItemDto::getMatchScore)
//                .average()
//                .orElse(0.0);
//        confidence += avgScore * 0.2;
//
//        return Math.min(confidence, 1.0);
//    }
//
//    private RecommendationResult createFallbackRecommendation(String query) {
//        // 오류 시 인기 상품 추천
//        List<Item> popularItems = itemRepository.findTop10ByIdNotOrderByWishCountDesc();
//        List<ItemDto> items = popularItems.stream()
//                .map(this::convertToDto)
//                .collect(Collectors.toList());
//
//        return new RecommendationResult(
//                query,
//                "FALLBACK",
//                new HashMap<>(),
//                items,
//                "요청을 정확히 이해하지 못해 인기 상품을 추천드립니다.",
//                0.3
//        );
//    }
//}
