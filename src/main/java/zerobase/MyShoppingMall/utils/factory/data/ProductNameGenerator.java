package zerobase.MyShoppingMall.utils.factory.data;

import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.type.*;

import java.util.*;

@Component
public class ProductNameGenerator {

    private final Map<Gender, List<String>> genderAdjectives = new HashMap<>();
    private final Map<ItemCategory, List<String>> categoryNames = new HashMap<>();
    private final Map<Styles, List<String>> styleAdjectives = new HashMap<>();
    private final List<String> brands = Arrays.asList(
            "에이랜드", "유니클로", "자라", "H&M", "무지", "스파오",
            "탑텐", "폴로", "갭", "리바이스", "나이키", "아디다스"
    );
    private final Random random = new Random();

    public ProductNameGenerator() {
        initializeData();
    }

    private void initializeData() {
        // 성별별 형용사
        genderAdjectives.put(Gender.MALE, Arrays.asList(
                "멋있는", "시크한", "클래식", "모던", "캐주얼", "스포티", "빈티지", "미니멀"
        ));
        genderAdjectives.put(Gender.FEMALE, Arrays.asList(
                "예쁜", "엘리건트", "큐트", "로맨틱", "시크", "트렌디", "페미닌", "보헤미안"
        ));

        // 카테고리별 아이템명
        categoryNames.put(ItemCategory.MENS_TOP, Arrays.asList(
                "긴팔 티셔츠", "맨투맨", "후드티", "셔츠", "니트", "카디건", "블레이저", "조끼"
        ));
        categoryNames.put(ItemCategory.WOMENS_TOP, Arrays.asList(
                "블라우스", "니트", "카디건", "크롭티", "셔츠", "튜닉", "베스트", "가디건"
        ));
        categoryNames.put(ItemCategory.MENS_BOTTOM, Arrays.asList(
                "청바지", "슬랙스", "조거팬츠", "반바지", "치노팬츠", "트레이닝복", "코듀로이"
        ));
        categoryNames.put(ItemCategory.WOMENS_BOTTOM, Arrays.asList(
                "스키니진", "와이드팬츠", "레깅스", "치마", "반바지", "플리츠스커트", "미니스커트"
        ));

        // 스타일별 형용사 (전체 등록)
        styleAdjectives.put(Styles.CASUAL, Arrays.asList("편안한", "데일리", "라이프"));
        styleAdjectives.put(Styles.FORMAL, Arrays.asList("정장", "비즈니스", "오피스"));
        styleAdjectives.put(Styles.SPORTY, Arrays.asList("스포츠", "액티브", "운동용"));
        styleAdjectives.put(Styles.VINTAGE, Arrays.asList("빈티지", "레트로", "클래식"));
        styleAdjectives.put(Styles.STREET, Arrays.asList("스트릿", "힙한", "트렌디"));
        styleAdjectives.put(Styles.DANDY, Arrays.asList("댄디한", "정제된", "깔끔한"));
        styleAdjectives.put(Styles.MINIMAL, Arrays.asList("미니멀", "심플", "단정한"));
        styleAdjectives.put(Styles.RETRO, Arrays.asList("복고풍", "레트로", "옛스러운"));
        styleAdjectives.put(Styles.LOVELY, Arrays.asList("러블리", "사랑스러운", "귀여운"));
        styleAdjectives.put(Styles.UNIQUE, Arrays.asList("독특한", "개성있는", "유니크"));
        styleAdjectives.put(Styles.CUTE, Arrays.asList("귀여운", "큐트", "사랑스러운"));
        styleAdjectives.put(Styles.CHIC, Arrays.asList("시크한", "도도한", "세련된"));
        styleAdjectives.put(Styles.CLEAN, Arrays.asList("깔끔한", "단정한", "모던"));
        styleAdjectives.put(Styles.SOFT, Arrays.asList("부드러운", "소프트", "편안한"));
        styleAdjectives.put(Styles.FRESH, Arrays.asList("산뜻한", "프레시", "밝은"));

        // 혹시 빠진 스타일이 있다면 기본 형용사로 채움
        for (Styles style : Styles.values()) {
            styleAdjectives.putIfAbsent(style, Arrays.asList("스타일리시", "베이직", "모던"));
        }
    }

    public String generateProductName(Gender gender, ItemCategory category, Styles style) {
        List<String> adjectives = genderAdjectives.getOrDefault(gender, Arrays.asList("멋진"));
        List<String> items = categoryNames.getOrDefault(category, Arrays.asList("의류"));
        List<String> styleAdjs = styleAdjectives.getOrDefault(style, Arrays.asList("스타일리시"));

        String adjective = adjectives.get(random.nextInt(adjectives.size()));
        String item = items.get(random.nextInt(items.size()));
        String styleAdj = styleAdjs.get(random.nextInt(styleAdjs.size()));

        // 다양한 패턴으로 상품명 생성
        switch (random.nextInt(4)) {
            case 0: return String.format("%s %s %s", adjective, styleAdj, item);
            case 1: return String.format("%s %s", styleAdj, item);
            case 2: return String.format("[%s] %s %s", getBrand(), adjective, item);
            default: return String.format("%s %s", adjective, item);
        }
    }

    public String generateProductDescription(String productName, Gender gender, Styles style, Season season) {
        List<String> descriptions = Arrays.asList(
                String.format("%s로 제작된 고품질 %s입니다.",
                        getRandomMaterial(), productName.toLowerCase()),
                String.format("%s 시즌에 어울리는 %s 스타일의 아이템입니다.",
                        getSeasonKorean(season), getStyleKorean(style)),
                String.format("편안한 착용감과 세련된 디자인이 특징인 %s입니다.", productName.toLowerCase()),
                String.format("다양한 코디에 활용 가능한 베이직 %s 아이템입니다.", productName.toLowerCase())
        );

        return descriptions.get(random.nextInt(descriptions.size()));
    }

    public String getBrand() {
        return brands.get(random.nextInt(brands.size()));
    }

    private String getRandomMaterial() {
        List<String> materials = Arrays.asList(
                "100% 코튼", "폴리에스터", "린넨", "울", "코튼 블렌드", "레이온", "스판덱스 혼방"
        );
        return materials.get(random.nextInt(materials.size()));
    }

    private String getSeasonKorean(Season season) {
        switch (season) {
            case SPRING: return "봄";
            case SUMMER: return "여름";
            case AUTUMN: return "가을";
            case WINTER: return "겨울";
            default: return "사계절";
        }
    }

    private String getStyleKorean(Styles style) {
        switch (style) {
            case CASUAL: return "캐주얼";
            case FORMAL: return "정장";
            case SPORTY: return "스포티";
            case VINTAGE: return "빈티지";
            case STREET: return "스트릿";
            default: return "모던";
        }
    }
}