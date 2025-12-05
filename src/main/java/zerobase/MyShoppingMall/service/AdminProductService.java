package zerobase.MyShoppingMall.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import zerobase.MyShoppingMall.dto.admin.ImageClassificationResult;
import zerobase.MyShoppingMall.dto.admin.ProductCreateRequest;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.service.item.S3UploadService;
import zerobase.MyShoppingMall.type.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminProductService {

    private final WebClient.Builder webClientBuilder;
    private final ItemRepository itemRepository;
    private final S3UploadService s3UploadService;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${file.upload-dir:/upload/images/}")
    private String uploadPath;

    /**
     * 이미지 업로드 → AI 분류
     */
    public ImageClassificationResult classifyImage(MultipartFile file) throws IOException {
        log.info("이미지 분류 요청: {}", file.getOriginalFilename());

        // 1. 이미지 임시 저장
        String savedPath = saveImage(file);

        // 2. AI 서비스 호출
        ImageClassificationResult result = callAiClassification(file);

        // 3. 이미지 경로 추가
        result.setImagePath(savedPath);
        result.setOriginalFileName(file.getOriginalFilename());

        log.info("분류 완료: category={}, itemName={}", result.getCategory(), result.getItemName());

        return result;
    }

    /**
     * AI 서비스 호출
     */
    private ImageClassificationResult callAiClassification(MultipartFile file) throws IOException {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            }).contentType(MediaType.parseMediaType(file.getContentType()));

            ImageClassificationResult result = webClientBuilder.build()
                    .post()
                    .uri(aiServiceUrl + "/classify/image")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(ImageClassificationResult.class)
                    .block();

            if (result == null) {
                throw new RuntimeException("AI 서비스 응답이 없습니다.");
            }

            return result;

        } catch (Exception e) {
            log.error("AI 서비스 호출 실패: {}", e.getMessage());

            return ImageClassificationResult.builder()
                    .category("MENS_TOP")
                    .subCategory("tshirt")
                    .gender("MALE")
                    .season("ALL_SEASON")
                    .style("CASUAL")
                    .primaryColor("BLACK")
                    .ageGroup("ADULT")
                    .itemName("새 상품")
                    .itemComment("상품 설명을 입력해주세요.")
                    .suggestedPrice(30000)
                    .analysisSuccess(false)
                    .build();
        }
    }

    /**
     * 이미지 저장
     */
    private String saveImage(MultipartFile file) throws IOException {
        String imageUrl = s3UploadService.uploadFile(file);
        log.info("이미지 S3 업로드 완료 :{}", imageUrl);
        return imageUrl;

//        Path uploadDir = Paths.get(uploadPath);
//        if (!Files.exists(uploadDir)) {
//            Files.createDirectories(uploadDir);
//        }
//
//        String originalFileName = file.getOriginalFilename();
//        String extension = "";
//        if (originalFileName != null && originalFileName.contains(".")) {
//            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//        }
//        String newFileName = UUID.randomUUID().toString() + extension;
//
//        Path filePath = uploadDir.resolve(newFileName);
//        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//        String webPath = "/upload/images/" + newFileName;
//        log.info("이미지 저장 완료: {}", webPath);
//
//        return webPath;
    }

    /**
     * 상품 등록 (DB 저장)
     */
    @Transactional
    public Item createProduct(ProductCreateRequest request) {
        log.info("상품등록 : {}", request.getItemName());

        // Enum 매핑
        Gender gender = mapGender(String.valueOf(request.getGender()));
        ItemCategory category = mapCategory(request.getCategory(), gender);
        ItemSubCategory subCategory = mapSubCategory(request.getSubCategory(), gender);
        Season season = mapSeason(String.valueOf(request.getSeason()));
        Styles style = mapStyle(String.valueOf(request.getStyle()));
        Color primaryColor = mapColor(String.valueOf(request.getPrimaryColor()));
        Color secondaryColor = mapColor(String.valueOf(request.getSecondaryColor()));
        AgeGroup ageGroup = mapAgeGroup(String.valueOf(request.getAgeGroup()));

        Item item = Item.builder()
                .itemName(request.getItemName())
                .itemComment(request.getItemComment())
                .price(request.getPrice())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 100)
                .category(category)
                .subCategory(subCategory)
                .gender(gender)
                .season(season)
                .style(style)
                .primaryColor(primaryColor)
                .secondaryColor(secondaryColor)
                .ageGroup(ageGroup)
                .imageUrl(request.getImagePath())
                .build();

        Item saved = itemRepository.save(item);
        log.info("상품 등록 완료: id={}", saved.getId());

        return saved;
    }

    /**
     * 성별 매핑
     */
    private Gender mapGender(String genderStr) {
        if (genderStr == null || genderStr.isEmpty()) {
            return Gender.UNISEX;
        }

        String upper = genderStr.toUpperCase();

        return switch (upper) {
            case "MALE", "MENS", "MEN", "M" -> Gender.MALE;
            case "FEMALE", "WOMENS", "WOMEN", "W" -> Gender.FEMALE;
            default -> Gender.UNISEX;
        };
    }

    /**
     * 카테고리 매핑 (성별 고려)
     */
    private ItemCategory mapCategory(String categoryStr, Gender gender) {
        if (categoryStr == null || categoryStr.isEmpty()) {
            return gender == Gender.FEMALE ? ItemCategory.WOMENS_TOP : ItemCategory.MENS_TOP;
        }

        String upper = categoryStr.toUpperCase();

        // 직접 매핑 시도
        try {
            return ItemCategory.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // 무시하고 아래 로직 진행
        }

        // 키워드 기반 매핑
        boolean isFemale = gender == Gender.FEMALE || upper.contains("WOMEN") || upper.contains("FEMALE");

        if (upper.contains("TOP") || upper.contains("SHIRT") || upper.contains("HOODIE")) {
            return isFemale ? ItemCategory.WOMENS_TOP : ItemCategory.MENS_TOP;
        }
        if (upper.contains("BOTTOM") || upper.contains("PANTS") || upper.contains("JEANS")) {
            return isFemale ? ItemCategory.WOMENS_BOTTOM : ItemCategory.MENS_BOTTOM;
        }
        if (upper.contains("OUTER") || upper.contains("JACKET") || upper.contains("COAT")) {
            return isFemale ? ItemCategory.WOMENS_OUTER : ItemCategory.MENS_OUTER;
        }
        if (upper.contains("SHOES") || upper.contains("SNEAKER") || upper.contains("BOOT")) {
            return isFemale ? ItemCategory.WOMENS_SHOES : ItemCategory.MENS_SHOES;
        }
        if (upper.contains("ACCESSOR") || upper.contains("WATCH") || upper.contains("RING")) {
            return isFemale ? ItemCategory.WOMENS_ACCESSORY : ItemCategory.MENS_ACCESSORY;
        }

        return isFemale ? ItemCategory.WOMENS_TOP : ItemCategory.MENS_TOP;
    }

    /**
     * 서브카테고리 매핑 (성별 고려)
     */
    private ItemSubCategory mapSubCategory(String subCategoryStr, Gender gender) {
        if (subCategoryStr == null || subCategoryStr.isEmpty()) {
            return gender == Gender.FEMALE ? ItemSubCategory.W_TSHIRT : ItemSubCategory.M_TSHIRT;
        }

        String lower = subCategoryStr.toLowerCase();
        boolean isFemale = gender == Gender.FEMALE;

        // 상의
        if (lower.contains("tshirt") || lower.contains("t-shirt") || lower.contains("shirt")) {
            return isFemale ? ItemSubCategory.W_TSHIRT : ItemSubCategory.M_TSHIRT;
        }
        if (lower.contains("hoodie")) {
            return isFemale ? ItemSubCategory.W_HOODIE : ItemSubCategory.M_HOODIE;
        }
        if (lower.contains("sweatshirt") || lower.contains("sweater")) {
            return isFemale ? ItemSubCategory.W_SWEATSHIRT : ItemSubCategory.M_SWEATSHIRT;
        }
        if (lower.contains("blouse")) {
            return ItemSubCategory.W_BLOUSE;
        }

        // 아우터
        if (lower.contains("coat")) {
            return isFemale ? ItemSubCategory.W_COAT : ItemSubCategory.M_COAT;
        }
        if (lower.contains("jacket") || lower.contains("windbreaker")) {
            return isFemale ? ItemSubCategory.W_WINDBREAKER : ItemSubCategory.M_WINDBREAKER;
        }
        if (lower.contains("padding") || lower.contains("puffer")) {
            return isFemale ? ItemSubCategory.W_PADDING : ItemSubCategory.M_PADDING;
        }

        // 하의
        if (lower.contains("jeans") || lower.contains("denim")) {
            return isFemale ? ItemSubCategory.W_JEANS : ItemSubCategory.M_JEANS;
        }
        if (lower.contains("jogger")) {
            return isFemale ? ItemSubCategory.W_JOGGER_PANTS : ItemSubCategory.M_JOGGER_PANTS;
        }
        if (lower.contains("training") || lower.contains("sweatpants")) {
            return isFemale ? ItemSubCategory.W_TRAINING_PANTS : ItemSubCategory.M_TRAINING_PANTS;
        }
        if (lower.contains("slacks") || lower.contains("dress_pants")) {
            return isFemale ? ItemSubCategory.W_SLACKS : ItemSubCategory.M_SLACKS;
        }
        if (lower.contains("shorts")) {
            return isFemale ? ItemSubCategory.W_SHORTS : ItemSubCategory.M_SHORTS;
        }
        if (lower.contains("skirt")) {
            return ItemSubCategory.W_SKIRT;
        }

        // 신발
        if (lower.contains("sneaker")) {
            return isFemale ? ItemSubCategory.W_SNEAKERS : ItemSubCategory.M_SNEAKERS;
        }
        if (lower.contains("running") || lower.contains("athletic")) {
            return isFemale ? ItemSubCategory.W_RUNNING_SHOES : ItemSubCategory.M_RUNNING_SHOES;
        }
        if (lower.contains("boot") || lower.contains("dress_shoe")) {
            return isFemale ? ItemSubCategory.W_BOOTS : ItemSubCategory.M_BOOTS;
        }

        // 악세서리
        if (lower.contains("watch")) {
            return isFemale ? ItemSubCategory.W_WATCH : ItemSubCategory.M_WATCH;
        }
        if (lower.contains("ring")) {
            return isFemale ? ItemSubCategory.W_RING : ItemSubCategory.M_RING;
        }
        if (lower.contains("necklace")) {
            return isFemale ? ItemSubCategory.W_NECKLACE : ItemSubCategory.M_NECKLACE;
        }

        // 기본값
        return isFemale ? ItemSubCategory.W_TSHIRT : ItemSubCategory.M_TSHIRT;
    }

    /**
     * 시즌 매핑
     */
    private Season mapSeason(String seasonStr) {
        if (seasonStr == null || seasonStr.isEmpty()) {
            return Season.ALL_SEASON;
        }

        String upper = seasonStr.toUpperCase();

        return switch (upper) {
            case "SPRING" -> Season.SPRING;
            case "SUMMER" -> Season.SUMMER;
            case "FALL", "AUTUMN" -> Season.AUTUMN;
            case "WINTER" -> Season.WINTER;
            default -> Season.ALL_SEASON;
        };
    }

    /**
     * 스타일 매핑
     */
    private Styles mapStyle(String styleStr) {
        if (styleStr == null || styleStr.isEmpty()) {
            return Styles.CASUAL;
        }

        String upper = styleStr.toUpperCase();

        return switch (upper) {
            case "CASUAL" -> Styles.CASUAL;
            case "FORMAL" -> Styles.FORMAL;
            case "SPORTY", "SPORT" -> Styles.SPORTY;
            case "STREET" -> Styles.STREET;
            case "MINIMAL" -> Styles.MINIMAL;
            case "VINTAGE", "RETRO" -> Styles.VINTAGE;
            case "CHIC" -> Styles.CHIC;
            case "LOVELY", "CUTE" -> Styles.CUTE;
            case "DANDY" -> Styles.DANDY;
            case "CLEAN" -> Styles.CLEAN;
            case "SOFT" -> Styles.SOFT;
            case "UNIQUE" -> Styles.UNIQUE;
            case "FRESH" -> Styles.FRESH;
            default -> Styles.CASUAL;
        };
    }

    /**
     * 색상 매핑
     */
    private Color mapColor(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) {
            return null;
        }

        String upper = colorStr.toUpperCase();

        // 직접 매핑 시도
        try {
            return Color.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // 유사 색상 매핑
            if (upper.contains("BLACK") || upper.contains("DARK")) return Color.BLACK;
            if (upper.contains("WHITE") || upper.contains("IVORY") || upper.contains("CREAM")) return Color.WHITE;
            if (upper.contains("GRAY") || upper.contains("GREY") || upper.contains("CHARCOAL")) return Color.GRAY;
            if (upper.contains("NAVY")) return Color.NAVY;
            if (upper.contains("BLUE") || upper.contains("DENIM")) return Color.BLUE;
            if (upper.contains("RED") || upper.contains("BURGUNDY") || upper.contains("WINE")) return Color.RED;
            if (upper.contains("PINK") || upper.contains("ROSE")) return Color.PINK;
            if (upper.contains("BEIGE") || upper.contains("TAN") || upper.contains("KHAKI")) return Color.BEIGE;
            if (upper.contains("BROWN") || upper.contains("CAMEL") || upper.contains("CHOCOLATE")) return Color.BROWN;
            if (upper.contains("GREEN") || upper.contains("OLIVE") || upper.contains("MINT")) return Color.GREEN;
            if (upper.contains("YELLOW") || upper.contains("GOLD") || upper.contains("MUSTARD")) return Color.YELLOW;
            if (upper.contains("ORANGE") || upper.contains("CORAL")) return Color.ORANGE;
            if (upper.contains("PURPLE") || upper.contains("VIOLET") || upper.contains("LAVENDER")) return Color.PURPLE;
            if (upper.contains("MULTI") || upper.contains("MIX") || upper.contains("PATTERN")) return Color.MULTI;

            log.warn("색상 매핑 실패: {} -> 기본값 BLACK 사용", colorStr);
            return Color.BLACK;
        }
    }

    /**
     * 연령대 매핑
     */
    private AgeGroup mapAgeGroup(String ageGroupStr) {
        if (ageGroupStr == null || ageGroupStr.isEmpty()) {
            return AgeGroup.ADULT;
        }

        String upper = ageGroupStr.toUpperCase();

        return switch (upper) {
            case "TEEN", "TEENAGER", "10S" -> AgeGroup.TEEN;
            case "TWENTY", "20S" -> AgeGroup.TWENTY;
            case "THIRTY", "30S" -> AgeGroup.THIRTY;
            case "FORTY", "40S", "MIDDLE_AGED" -> AgeGroup.FORTY;
            default -> AgeGroup.ADULT;
        };
    }

    /**
     * 이미지 삭제
     */
    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return;

        s3UploadService.deleteFile(imagePath);
        log.info("S3 이미지 삭제 완료: {}", imagePath);

//        try {
//            String filename = imagePath.substring(imagePath.lastIndexOf("/") + 1);
//            Path filePath = Paths.get(uploadPath).resolve(filename);
//            Files.deleteIfExists(filePath);
//            log.info("이미지 삭제: {}", imagePath);
//        } catch (IOException e) {
//            log.error("이미지 삭제 실패: {}", e.getMessage());
//        }
    }
}