package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.*;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminImageUploader {

    private final ItemService itemService;
    @Qualifier("aiRestTemplate")
    private final RestTemplate restTemplate;

    @Value("${ai.classifier.url:http://localhost:5000}")
    private String aiServerUrl;

    /**
     * 이미지 업로더 페이지
     */
    @GetMapping("/imageUploader")
    public String imageUploaderPage(Model model) {
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("genders", Gender.values());
        return "admin/imageUploader/upload";
    }

    /**
     * AI 이미지 분류 및 자동 상품 등록
     */
    @PostMapping("/upload-and-classify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAndClassifyImage(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("itemName") String itemName,
            @RequestParam("itemComment") String itemComment,
            @RequestParam("price") Integer price,
            @RequestParam("quantity") Integer quantity) {

        try {
            // 1. 이미지 파일 검증
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미지 파일을 선택해주세요."));
            }

            // 2. Flask AI 서버로 이미지 전송하여 분류
            log.info("AI 서버로 이미지 분류 요청 시작");
            Map<String, Object> classificationResult = classifyImageWithAI(imageFile);

            if (!(Boolean) classificationResult.get("success")) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "AI 분류 실패: " + classificationResult.get("message")));
            }

            // 3. AI 분류 결과로 ItemRequestDto 생성
            ItemRequestDto itemDto = createItemDtoFromAI(
                    classificationResult, itemName, itemComment, price, quantity, imageFile);

            // 4. 상품 생성
            var createdItem = itemService.createItem(itemDto);

            log.info("AI 자동 분류 상품 등록 완료: {}", createdItem.getItemName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상품이 성공적으로 등록되었습니다.",
                    "item", createdItem,
                    "classification", classificationResult.get("data")
            ));

        } catch (Exception e) {
            log.error("AI 이미지 분류 및 상품 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "상품 등록 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 수동 상품 등록 (기존 방식)
     */
    @PostMapping("/upload-manual")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadManualItem(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("itemName") String itemName,
            @RequestParam("itemComment") String itemComment,
            @RequestParam("price") Integer price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("category") ItemCategory category,
            @RequestParam("gender") Gender gender) {

        try {
            // 수동으로 설정된 정보로 DTO 생성
            ItemRequestDto itemDto = ItemRequestDto.builder()
                    .itemName(itemName)
                    .itemComment(itemComment)
                    .price(price)
                    .quantity(quantity)
                    .category(category)
                    .gender(gender)
                    .imageFile(imageFile)
                    .build();

            var createdItem = itemService.createItem(itemDto);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "상품이 성공적으로 등록되었습니다.",
                    "item", createdItem
            ));

        } catch (Exception e) {
            log.error("수동 상품 등록 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "상품 등록 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 이미지만 분류 테스트 (상품 등록 없이)
     */
    @PostMapping("/classify-only")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> classifyImageOnly(
            @RequestParam("imageFile") MultipartFile imageFile) {

        try {
            Map<String, Object> result = classifyImageWithAI(imageFile);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("이미지 분류 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "이미지 분류 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * Flask AI 서버로 이미지 분류 요청
     */
    private Map<String, Object> classifyImageWithAI(MultipartFile imageFile) throws Exception {
        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // MultiValueMap으로 파일 전송
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", imageFile.getResource());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Flask 서버로 요청
        String url = aiServerUrl + "/classify-image";
        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("AI 서버 응답 오류: " + response.getStatusCode());
        }
    }

    /**
     * AI 분류 결과로 ItemRequestDto 생성
     */
    private ItemRequestDto createItemDtoFromAI(
            Map<String, Object> classificationResult,
            String itemName,
            String itemComment,
            Integer price,
            Integer quantity,
            MultipartFile imageFile) {

        Map<String, Object> data = (Map<String, Object>) classificationResult.get("data");

        // AI 결과를 enum으로 변환
        ItemCategory category = parseCategory((String) data.get("category"));
        Gender gender = parseGender((String) data.get("gender"));

        return ItemRequestDto.builder()
                .itemName(itemName)
                .itemComment(itemComment)
                .price(price)
                .quantity(quantity)
                .category(category)
                .gender(gender)
                .imageFile(imageFile)
                .subCategory(parseSubCategory((String) data.get("subCategory")))
                .ageGroup(parseAgeGroup((String) data.get("ageGroup")))
                .style(parseStyle((String) data.get("style")))
                .season(parseSeason((String) data.get("season")))
                .primaryColor(parseColor((String) data.get("color")))
                .build();
    }

    /**
     * 문자열을 ItemCategory enum으로 변환
     */
    private ItemCategory parseCategory(String category) {
        try {
            return ItemCategory.valueOf(category.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 카테고리: {}, 기본값 사용", category);
            return ItemCategory.MENS_TOP; // 기본값
        }
    }

    /**
     * 문자열을 Gender enum으로 변환
     */
    private Gender parseGender(String gender) {
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 성별: {}, 기본값 사용", gender);
            return Gender.UNISEX; // 기본값
        }
    }

    private ItemSubCategory parseSubCategory(String subCategory) {
        try {
            return ItemSubCategory.valueOf(subCategory.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 서브카테고리: {}, 기본값 사용", subCategory);
            return ItemSubCategory.W_TSHIRT; // 기본값 지정
        }
    }

    private AgeGroup parseAgeGroup(String ageGroup) {
        try {
            return AgeGroup.valueOf(ageGroup.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 연령대: {}, 기본값 사용", ageGroup);
            return AgeGroup.TEEN; // 기본값
        }
    }

    private Styles parseStyle(String style) {
        try {
            return Styles.valueOf(style.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 스타일: {}, 기본값 사용", style);
            return Styles.CASUAL; // 기본값
        }
    }

    private Season parseSeason(String season) {
        try {
            return Season.valueOf(season.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 시즌: {}, 기본값 사용", season);
            return Season.SPRING; // 기본값
        }
    }

    private Color parseColor(String color) {
        try {
            return Color.valueOf(color.toUpperCase());
        } catch (Exception e) {
            log.warn("알 수 없는 색상: {}, 기본값 사용", color);
            return Color.BLACK; // 기본값
        }
    }
}
