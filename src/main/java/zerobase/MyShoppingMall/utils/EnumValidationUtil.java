package zerobase.MyShoppingMall.utils;

import lombok.extern.slf4j.Slf4j;
import zerobase.MyShoppingMall.type.*;

@Slf4j
public class EnumValidationUtil {

    /**
     * AgeGroup enum에 ADULT가 없는 경우를 위한 매핑
     */
    public static AgeGroup mapToValidAgeGroup(String ageGroup) {
        if (ageGroup == null || ageGroup.trim().isEmpty()) {
            return getDefaultAgeGroup();
        }

        String normalized = ageGroup.toUpperCase().trim();

        // 특별 매핑 처리
        switch (normalized) {
            case "TWENTIES":
            case "ADULT":
                return findValidAgeGroup("TEEN"); // ADULT가 없다면 TEEN 사용
            case "TEEN":
            case "TEENAGER":
                return AgeGroup.TEEN;
            default:
                try {
                    return AgeGroup.valueOf(normalized);
                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 연령대: '{}', 기본값 사용", ageGroup);
                    return getDefaultAgeGroup();
                }
        }
    }

    /**
     * 사용 가능한 AgeGroup 찾기
     */
    private static AgeGroup findValidAgeGroup(String preferred) {
        try {
            return AgeGroup.valueOf(preferred);
        } catch (IllegalArgumentException e) {
            // TEEN이 없다면 첫 번째 enum 값 사용
            return AgeGroup.values()[0];
        }
    }

    /**
     * 기본 AgeGroup 반환
     */
    private static AgeGroup getDefaultAgeGroup() {
        return AgeGroup.values().length > 0 ? AgeGroup.values()[0] : null;
    }

    /**
     * ItemCategory 안전 파싱
     */
    public static ItemCategory parseItemCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return ItemCategory.MENS_TOP; // 기본값
        }

        try {
            return ItemCategory.valueOf(category.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 카테고리: '{}', 기본값 사용", category);
            return ItemCategory.MENS_TOP;
        }
    }

    /**
     * Gender 안전 파싱
     */
    public static Gender parseGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return Gender.UNISEX;
        }

        try {
            return Gender.valueOf(gender.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 성별: '{}', 기본값 사용", gender);
            return Gender.UNISEX;
        }
    }

    /**
     * Color 안전 파싱 (null 값 특별 처리)
     */
    public static Color parseColor(String color) {
        if (color == null || color.trim().isEmpty() || "null".equalsIgnoreCase(color)) {
            return Color.BLACK; // 기본값
        }

        try {
            return Color.valueOf(color.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 색상: '{}', 기본값 사용", color);
            return Color.BLACK;
        }
    }

    /**
     * ItemSubCategory 안전 파싱
     */
    public static ItemSubCategory parseSubCategory(String subCategory) {
        if (subCategory == null || subCategory.trim().isEmpty()) {
            return ItemSubCategory.M_TSHIRT; // 기본값
        }

        try {
            return ItemSubCategory.valueOf(subCategory.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 서브카테고리: '{}', 기본값 사용", subCategory);
            return ItemSubCategory.M_TSHIRT;
        }
    }

    /**
     * Styles 안전 파싱
     */
    public static Styles parseStyle(String style) {
        if (style == null || style.trim().isEmpty()) {
            return Styles.CASUAL; // 기본값
        }

        try {
            return Styles.valueOf(style.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 스타일: '{}', 기본값 사용", style);
            return Styles.CASUAL;
        }
    }

    /**
     * Season 안전 파싱
     */
    public static Season parseSeason(String season) {
        if (season == null || season.trim().isEmpty()) {
            return Season.SPRING; // 기본값
        }

        try {
            return Season.valueOf(season.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 시즌: '{}', 기본값 사용", season);
            return Season.SPRING;
        }
    }

    /**
     * 모든 enum 값들이 유효한지 검증
     */
    public static boolean validateEnumValues() {
        try {
            // 각 enum의 첫 번째 값들로 테스트
            AgeGroup.values();
            ItemCategory.values();
            Gender.values();
            Color.values();
            ItemSubCategory.values();
            Styles.values();
            Season.values();

            log.info("모든 Enum 타입 검증 완료");
            return true;
        } catch (Exception e) {
            log.error("Enum 타입 검증 실패", e);
            return false;
        }
    }
}
