package zerobase.MyShoppingMall.utils.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.type.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ItemDataFactory extends BaseDataFactory<Item> {

    @Override
    public Item createSingle() {
        Gender gender = randomEnum(Gender.class);
        ItemCategory category = generateCategoryByGender(gender);
        ItemSubCategory subCategory = generateSubCategoryByCategory(category);
        Styles style = randomEnum(Styles.class);
        Season season = randomEnum(Season.class);
        AgeGroup ageGroup = randomEnum(AgeGroup.class);

        String productName = productNameGenerator.generateProductName(gender, category, style);
        String description = productNameGenerator.generateProductDescription(productName, gender, style, season);

        int basePrice = generateRealisticPrice(category, style);
        int quantity = generateRealisticQuantity();

        // 통계 데이터 (서로 연관성 있게 생성)
        int viewCount = generateViewCount();
        int cartCount = (int) (viewCount * (0.1 + random.nextDouble() * 0.2)); // 10-30% 장바구니 추가
        int orderCount = (int) (cartCount * (0.3 + random.nextDouble() * 0.4)); // 30-70% 구매
        int reviewCount = (int) (orderCount * (0.2 + random.nextDouble() * 0.6)); // 20-80% 리뷰
        int wishCount = (int) (viewCount * (0.05 + random.nextDouble() * 0.15)); // 5-20% 위시리스트

        double rating = generateRealisticRating(reviewCount);

        return Item.builder()
                .itemName(productName)
                .itemComment(description)
                .gender(gender)
                .category(category)
                .subCategory(subCategory)
                .style(style)
                .season(season)
                .ageGroup(ageGroup)
                .price(basePrice)
                .quantity(quantity)
                .viewCount(viewCount)
                .cartCount(cartCount)
                .orderCount(orderCount)
                .reviewCount(reviewCount)
                .wishCount(wishCount)
                .itemRating((int) rating)
                .deleteType('N')
                .createdAt(generateRealisticCreatedDate())
                .build();
    }

    public List<Item> createBatch(int size) {
        List<Item> items = super.createBatch(size);
        log.info("🧪 ItemDataFactory → createBatch({}) → 실제 생성: {}개", size, items.size());
        return items;
    }


    @Override
    public Item createWithSpecificData(Object... params) {
        return null;
    }

    private ItemCategory generateCategoryByGender(Gender gender) {
        ItemCategory[] categories;

        if (gender == Gender.MALE) {
            categories = new ItemCategory[]{
                    ItemCategory.MENS_TOP,
                    ItemCategory.MENS_BOTTOM,
                    ItemCategory.MENS_OUTER,
                    ItemCategory.MENS_SHOES,
                    ItemCategory.MENS_ACCESSORY
            };
        } else {
            categories = new ItemCategory[]{
                    ItemCategory.WOMENS_TOP,
                    ItemCategory.WOMENS_BOTTOM,
                    ItemCategory.WOMENS_OUTER,
                    ItemCategory.WOMENS_SHOES,
                    ItemCategory.WOMENS_ACCESSORY
            };
        }

        return categories[random.nextInt(categories.length)];
    }

    private ItemSubCategory generateSubCategoryByCategory(ItemCategory category) {
        switch (category) {
            case MENS_TOP:
                ItemSubCategory[] mensTops = {
                        ItemSubCategory.M_TSHIRT, ItemSubCategory.M_SWEATSHIRT,
                        ItemSubCategory.M_SHIRT, ItemSubCategory.M_HOODIE,
                        ItemSubCategory.M_SHORTS
                };
                return mensTops[random.nextInt(mensTops.length)];

            case WOMENS_TOP:
                ItemSubCategory[] womensTops = {
                        ItemSubCategory.W_TSHIRT, ItemSubCategory.W_SWEATSHIRT,
                        ItemSubCategory.W_BLOUSE, ItemSubCategory.W_HOODIE,
                        ItemSubCategory.W_SHORTS
                };
                return womensTops[random.nextInt(womensTops.length)];

            case MENS_BOTTOM:
                ItemSubCategory[] mensBottoms = {
                        ItemSubCategory.M_JEANS,
                        ItemSubCategory.M_TRAINING_PANTS,
                        ItemSubCategory.M_JOGGER_PANTS,
                        ItemSubCategory.M_SLACKS
                };
                return mensBottoms[random.nextInt(mensBottoms.length)];

            case WOMENS_BOTTOM:
                ItemSubCategory[] womensBottoms = {
                        ItemSubCategory.W_JEANS,
                        ItemSubCategory.W_TRAINING_PANTS,
                        ItemSubCategory.W_JOGGER_PANTS,
                        ItemSubCategory.W_SKIRTS,
                        ItemSubCategory.W_SKIRT,
                        ItemSubCategory.W_SLACKS
                };
                return womensBottoms[random.nextInt(womensBottoms.length)];

            case MENS_OUTER:
                ItemSubCategory[] mensOuters = {
                        ItemSubCategory.M_WINDBREAKER,
                        ItemSubCategory.M_COAT,
                        ItemSubCategory.M_PADDING
                };
                return mensOuters[random.nextInt(mensOuters.length)];

            case WOMENS_OUTER:
                ItemSubCategory[] womensOuters = {
                        ItemSubCategory.W_WINDBREAKER,
                        ItemSubCategory.W_COAT,
                        ItemSubCategory.W_PADDING
                };
                return womensOuters[random.nextInt(womensOuters.length)];

            case MENS_SHOES:
                ItemSubCategory[] mensShoes = {
                        ItemSubCategory.M_SNEAKERS,
                        ItemSubCategory.M_RUNNING_SHOES,
                        ItemSubCategory.M_BOOTS
                };
                return mensShoes[random.nextInt(mensShoes.length)];

            case WOMENS_SHOES:
                ItemSubCategory[] womensShoes = {
                        ItemSubCategory.W_SNEAKERS,
                        ItemSubCategory.W_RUNNING_SHOES,
                        ItemSubCategory.W_BOOTS
                };
                return womensShoes[random.nextInt(womensShoes.length)];

            case MENS_ACCESSORY:
                ItemSubCategory[] mensAccessories = {
                        ItemSubCategory.M_WATCH,
                        ItemSubCategory.M_RING,
                        ItemSubCategory.M_NECKLACE
                };
                return mensAccessories[random.nextInt(mensAccessories.length)];

            case WOMENS_ACCESSORY:
                ItemSubCategory[] womensAccessories = {
                        ItemSubCategory.W_WATCH,
                        ItemSubCategory.W_RING,
                        ItemSubCategory.W_NECKLACE
                };
                return womensAccessories[random.nextInt(womensAccessories.length)];

            default:
                return ItemSubCategory.M_TSHIRT; // fallback
        }
    }

    private int generateRealisticPrice(ItemCategory category, Styles style) {
        int basePrice;

        // 카테고리별 기본 가격대
        switch (category) {
            case MENS_SHOES:
            case WOMENS_SHOES:
                basePrice = 15000 + random.nextInt(35000); // 15,000 ~ 50,000
                break;
            case MENS_ACCESSORY:
            case WOMENS_ACCESSORY:
                basePrice = 25000 + random.nextInt(55000); // 25,000 ~ 80,000
                break;
            case MENS_OUTER:
            case WOMENS_OUTER:
                basePrice = 35000 + random.nextInt(35000);
                break;
            default:
                basePrice = 20000 + random.nextInt(30000);
        }

        // 스타일별 가격 조정
        switch (style) {
            case FORMAL:
                basePrice = (int) (basePrice * 1.5); // 정장은 50% 더 비싸게
                break;
            case VINTAGE:
                basePrice = (int) (basePrice * 1.2); // 빈티지는 20% 더 비싸게
                break;
            case CASUAL:
                basePrice = (int) (basePrice * 0.8); // 캐주얼은 20% 더 싸게
                break;
        }

        // 1000원 단위로 조정
        return (basePrice / 1000) * 1000;
    }

    private int generateRealisticQuantity() {
        // 재고 분포를 현실적으로
        double rand = random.nextDouble();
        if (rand < 0.1) return 0; // 10% 품절
        if (rand < 0.3) return 1 + random.nextInt(5); // 20% 소량 재고
        if (rand < 0.8) return 10 + random.nextInt(40); // 50% 일반 재고
        return 50 + random.nextInt(100); // 20% 대량 재고
    }

    private int generateViewCount() {
        // 로그 정규분포를 따르는 조회수
        double logNormal = Math.exp(random.nextGaussian() * 0.5 + 4); // 평균 약 100
        return Math.max(1, (int) logNormal);
    }

    private double generateRealisticRating(int reviewCount) {
        if (reviewCount == 0) return 0.0;

        // 평점은 보통 3.0 ~ 5.0 사이에 몰림
        double baseRating = 3.0 + random.nextGaussian() * 0.8;
        baseRating = Math.max(1.0, Math.min(5.0, baseRating));

        // 소수점 한 자리로 반올림
        return Math.round(baseRating * 10.0) / 10.0;
    }

    private LocalDateTime generateRealisticCreatedDate() {
        // 최근 1년 내의 날짜 생성 (신상품이 더 많도록)
        int daysAgo = (int) Math.abs(random.nextGaussian() * 60); // 평균 60일 전
        daysAgo = Math.min(365, daysAgo); // 최대 1년 전

        return LocalDateTime.now().minusDays(daysAgo);
    }
}

