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

            // 파일 크기 검증
            if (imageFile.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미지 파일 크기는 10MB를 초과할 수 없습니다."));
            }

            // 2. Flask AI 서버로 이미지 전송하여 분류
            log.info("AI 서버로 이미지 분류 요청 시작: {}", imageFile.getOriginalFilename());
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

            log.info("AI 자동 분류 상품 등록 완료: {}, 카테고리: {}",
                    createdItem.getItemName(), itemDto.getCategory());

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
     * Flask AI 서버로 이미지 분류 요청
     */
    private Map<String, Object> classifyImageWithAI(MultipartFile imageFile) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Accept", "application/json");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", imageFile.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = aiServerUrl + "/classify-image";
            log.info("AI 서버 요청 URL: {}, 파일명: {}", url, imageFile.getOriginalFilename());

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, Map.class);

            log.info("AI 서버 응답 상태: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                log.info("AI 분류 결과: {}", responseBody);
                return responseBody;
            } else {
                throw new RuntimeException("AI 서버 응답 오류: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("AI 서버 통신 실패: {}", e.getMessage());
            throw new RuntimeException("AI 서버와의 통신에 실패했습니다: " + e.getMessage());
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

        ItemCategory category = parseCategory((String) data.get("category"));
        Gender gender = parseGender((String) data.get("gender"));
        ItemSubCategory subCategory = parseSubCategory((String) data.get("subCategory"));
        AgeGroup ageGroup = parseAgeGroupSafely((String) data.get("ageGroup"));
        Styles style = parseStyle((String) data.get("style"));
        Season season = parseSeason((String) data.get("season"));
        Color primaryColor = parseColor((String) data.get("color"));

        log.info("AI 분류 결과 변환 완료 - 카테고리: {}, 성별: {}, 연령대: {}, 색상: {}",
                category, gender, ageGroup, primaryColor);

        return ItemRequestDto.builder()
                .itemName(itemName)
                .itemComment(itemComment)
                .price(price)
                .quantity(quantity)
                .category(category)
                .gender(gender)
                .imageFile(imageFile)
                .subCategory(subCategory)
                .ageGroup(ageGroup)
                .style(style)
                .season(season)
                .primaryColor(primaryColor)
                .build();
    }


    private ItemCategory parseCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            log.warn("카테고리가 null이거나 빈 값입니다. 기본값 사용");
            return ItemCategory.MENS_TOP;
        }
        try {
            return ItemCategory.valueOf(category.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 카테고리: '{}', 기본값 사용", category);
            return ItemCategory.MENS_TOP;
        }
    }

    private Gender parseGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            log.warn("성별이 null이거나 빈 값입니다. 기본값 사용");
            return Gender.UNISEX;
        }
        try {
            return Gender.valueOf(gender.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 성별: '{}', 기본값 사용", gender);
            return Gender.UNISEX;
        }
    }

    private ItemSubCategory parseSubCategory(String subCategory) {
        if (subCategory == null || subCategory.trim().isEmpty()) {
            log.warn("서브카테고리가 null이거나 빈 값입니다. 기본값 사용");
            return ItemSubCategory.M_TSHIRT;
        }
        try {
            return ItemSubCategory.valueOf(subCategory.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 서브카테고리: '{}', 기본값 사용", subCategory);
            return ItemSubCategory.M_TSHIRT;
        }
    }


    private AgeGroup parseAgeGroupSafely(String ageGroup) {
        if (ageGroup == null || ageGroup.trim().isEmpty()) {
            log.warn("연령대가 null이거나 빈 값입니다. 기본값 사용");
            return getFirstAvailableAgeGroup();
        }

        String normalized = ageGroup.toUpperCase().trim();
        log.info("연령대 파싱 시도: '{}'", normalized);

        // 사용 가능한 AgeGroup 값들을 순서대로 시도
        String[] possibleValues = {"TEEN", "ADULT", "CHILD", "SENIOR", "ALL"};

        for (String value : possibleValues) {
            try {
                AgeGroup result = AgeGroup.valueOf(value);
                log.info("연령대 파싱 성공: {} -> {}", normalized, result);
                return result;
            } catch (IllegalArgumentException e) {
                // 이 값이 없으면 다음 값 시도
            }
        }

        // 모든 값이 실패하면 첫 번째 enum 값 사용
        AgeGroup fallback = getFirstAvailableAgeGroup();
        log.warn("모든 연령대 파싱 실패, 첫 번째 enum 값 사용: {}", fallback);
        return fallback;
    }


    private AgeGroup getFirstAvailableAgeGroup() {
        AgeGroup[] values = AgeGroup.values();
        if (values.length > 0) {
            return values[0];
        }
        throw new RuntimeException("AgeGroup enum에 사용 가능한 값이 없습니다.");
    }

    private Styles parseStyle(String style) {
        if (style == null || style.trim().isEmpty()) {
            log.warn("스타일이 null이거나 빈 값입니다. 기본값 사용");
            return Styles.CASUAL;
        }
        try {
            return Styles.valueOf(style.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 스타일: '{}', 기본값 사용", style);
            return Styles.CASUAL;
        }
    }

    private Season parseSeason(String season) {
        if (season == null || season.trim().isEmpty()) {
            log.warn("시즌이 null이거나 빈 값입니다. 기본값 사용");
            return Season.SPRING;
        }
        try {
            return Season.valueOf(season.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 시즌: '{}', 기본값 사용", season);
            return Season.SPRING;
        }
    }

    private Color parseColor(String color) {
        if (color == null || color.trim().isEmpty() || "null".equalsIgnoreCase(color)) {
            log.warn("색상이 null이거나 빈 값입니다. 기본값 사용");
            return Color.BLACK;
        }
        try {
            return Color.valueOf(color.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 색상: '{}', 기본값 사용", color);
            return Color.BLACK;
        }
    }

    @GetMapping("/debug-enums")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> debugEnums() {
        try {
            Map<String, Object> enumInfo = Map.of(
                    "AgeGroup", java.util.Arrays.toString(AgeGroup.values()),
                    "ItemCategory", java.util.Arrays.toString(ItemCategory.values()),
                    "Gender", java.util.Arrays.toString(Gender.values()),
                    "Color", java.util.Arrays.toString(Color.values()),
                    "ItemSubCategory", java.util.Arrays.toString(ItemSubCategory.values()),
                    "Styles", java.util.Arrays.toString(Styles.values()),
                    "Season", java.util.Arrays.toString(Season.values())
            );

            log.info("Enum 값들: {}", enumInfo);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "enums", enumInfo
            ));
        } catch (Exception e) {
            log.error("Enum 디버깅 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * AI 서버 상태
     */
    @GetMapping("/ai-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkAiServerStatus() {
        try {
            String url = aiServerUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "AI 서버가 정상 작동 중입니다.",
                        "serverInfo", response.getBody()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("success", false, "message", "AI 서버에 연결할 수 없습니다."));
            }
        } catch (Exception e) {
            log.error("AI 서버 상태 확인 실패", e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("success", false, "message", "AI 서버 연결 실패: " + e.getMessage()));
        }
    }

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
     * 이미지만 분류 테스트
     */
    @PostMapping("/classify-only")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> classifyImageOnly(
            @RequestParam("imageFile") MultipartFile imageFile) {

        try {
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "이미지 파일을 선택해주세요."));
            }

            Map<String, Object> result = classifyImageWithAI(imageFile);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("이미지 분류 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "이미지 분류 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}