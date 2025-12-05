package zerobase.MyShoppingMall.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageClassificationResult {

    // 기본 분류(HuggingFace)
    private String basicCategory;
    private String basicType;
    private Double basicConfidence;

    // 상세 분석(OpenAI Vision)
    private String category;
    private String subCategory;
    private String gender;
    private String season;
    private String style;
    private String primaryColor;
    private String secondaryColor;
    private String ageGroup;

    // 생성된 정보
    private String itemName;
    private String itemComment;
    private Integer suggestedPrice;
    private List<String> keywords;

    // 메타 정보
    private Boolean analysisSuccess;

    // 이미지 정보
    private String imagePath;
    private String originalFileName;
}
