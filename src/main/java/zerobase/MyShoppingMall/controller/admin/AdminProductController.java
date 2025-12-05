package zerobase.MyShoppingMall.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.dto.admin.ImageClassificationResult;
import zerobase.MyShoppingMall.dto.admin.ProductCreateRequest;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.service.AdminProductService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService adminProductService;

    /**
     * 이미지 업로드 -> AI 분류
     */
    @PostMapping("/classify")
    public ResponseEntity<ImageClassificationResult> classifyImage(
            @RequestParam("file") MultipartFile file) {
        log.info("이미지 분류 요청 :{}", file.getOriginalFilename());

        try {
            ImageClassificationResult result = adminProductService.classifyImage(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("이미지 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 상품 등록(분류 결과 확인 후에)
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createProduct(
            @RequestBody ProductCreateRequest request) {

        log.info("상품 등록 요청: {}", request.getItemName());

        try {
            Item item = adminProductService.createProduct(request);

            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            map.put("message", "상품이 등록되었습니다,");
            map.put("itemId", item.getId());
            map.put("itemName", item.getItemName());

            return ResponseEntity.ok(map);
        } catch (Exception e) {
            log.error("상품 등록 실패: {}", e.getMessage());

            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("message", "상품 등록에 실패했습니다." + e.getMessage());

            return ResponseEntity.badRequest().body(map);
        }
    }

    /**
     * 분류 취소 (이미지 삭제)
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelClassification(
            @RequestParam("imagePath") String imagePath) {
        log.info("분류 취소 요청: {}", imagePath);

        adminProductService.deleteImage(imagePath);

        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", "취소되었습니다.");

        return ResponseEntity.ok(map);
    }

    /**
     * 일괄 업로드 (이미지 여러개)
     */
    @PostMapping("/batch-classify")
    public ResponseEntity<Map<String, Object>> batchClassify(
            @RequestParam("files") MultipartFile[] files) {
        log.info("일괄 분류 요청: {}개 파일", files.length);

        Map<String, Object> map = new HashMap<>();
        Map<String, ImageClassificationResult> resultMap = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            try {
                ImageClassificationResult result = adminProductService.classifyImage(file);
                resultMap.put(result.getOriginalFileName(), result);
                successCount++;
            } catch (Exception e) {
                log.error("분류 실패 - {}: {}", file.getOriginalFilename(), e.getMessage());
                failCount++;
            }
        }

        map.put("success", true);
        map.put("totalCount", files.length);
        map.put("successCount", successCount);
        map.put("failCount", failCount);
        map.put("results", resultMap);

        return ResponseEntity.ok(map);
    }


}
