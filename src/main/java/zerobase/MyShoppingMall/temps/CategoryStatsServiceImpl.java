package zerobase.MyShoppingMall.temps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryStatsServiceImpl implements CategoryStatsService {
    @Override
    public CategoryStats getCategoryStats() {
        try {
            // 임시 기본 데이터 반환
            CategoryStats stats = new CategoryStats();

            // 기본 차트 데이터 (JSON 형태)
            stats.setChartData("{\"labels\":[\"상의\",\"하의\",\"아우터\",\"신발\"],\"data\":[30,25,20,25],\"colors\":[\"#4f46e5\",\"#7c3aed\",\"#10b981\",\"#f59e0b\"]}");

            // 기본 카테고리 정보
            List<CategoryInfo> categories = Arrays.asList(
                    new CategoryInfo("상의", 100, 1500, 30.0, "#4f46e5"),
                    new CategoryInfo("하의", 80, 1200, 25.0, "#7c3aed"),
                    new CategoryInfo("아우터", 60, 900, 20.0, "#10b981"),
                    new CategoryInfo("신발", 90, 1100, 25.0, "#f59e0b")
            );
            stats.setCategories(categories);

            log.debug("CategoryStats 기본 데이터 반환");
            return stats;

        } catch (Exception e) {
            log.warn("CategoryStats 조회 실패, 빈 데이터 반환: {}", e.getMessage());
            return createEmptyStats();
        }
    }

    private CategoryStats createEmptyStats() {
        CategoryStats emptyStats = new CategoryStats();
        emptyStats.setChartData("{}");
        return emptyStats;
    }
}
