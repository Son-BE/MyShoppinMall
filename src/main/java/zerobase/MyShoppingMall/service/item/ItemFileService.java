package zerobase.MyShoppingMall.service.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemFileService {

    private final S3UploadService s3UploadService;

    // 허용된 이미지 확장자
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    );

    // 최대 파일 크기 (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 아이템 이미지 파일 업로드
     */
    public String uploadItemImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            log.info("업로드할 이미지 파일이 없음");
            return null;
        }

        validateImageFile(imageFile);

        try {
            String imageUrl = s3UploadService.uploadFile(imageFile);
            log.info("이미지 업로드 성공, URL: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("이미지 업로드 실패, 파일명: {}, 예외: {}",
                    imageFile.getOriginalFilename(), e.getMessage());
            throw new IOException("이미지 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 기존 이미지 삭제 후 새 이미지 업로드
     */
    public String replaceItemImage(String oldImageUrl, MultipartFile newImageFile) throws IOException {
        if (newImageFile == null || newImageFile.isEmpty()) {
            log.info("새 이미지 파일이 없어 기존 이미지 유지");
            return oldImageUrl;
        }

        validateImageFile(newImageFile);

        try {
            // 새 이미지 업로드
            String newImageUrl = s3UploadService.uploadFile(newImageFile);

            // 기존 이미지 삭제 (S3에서)
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                deleteImageFromS3(oldImageUrl);
            }

            log.info("이미지 교체 성공, 기존: {}, 새로운: {}", oldImageUrl, newImageUrl);
            return newImageUrl;
        } catch (IOException e) {
            log.error("이미지 교체 실패, 파일명: {}, 예외: {}",
                    newImageFile.getOriginalFilename(), e.getMessage());
            throw new IOException("이미지 교체에 실패했습니다.", e);
        }
    }

    /**
     * S3에서 이미지 삭제
     */
    public void deleteItemImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.info("삭제할 이미지 URL이 없음");
            return;
        }

        try {
            deleteImageFromS3(imageUrl);
            log.info("이미지 삭제 성공, URL: {}", imageUrl);
        } catch (Exception e) {
            log.error("이미지 삭제 실패, URL: {}, 예외: {}", imageUrl, e.getMessage());
        }
    }

    /**
     * 이미지 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile imageFile) throws IOException {
        // 파일 크기 검사
        if (imageFile.getSize() > MAX_FILE_SIZE) {
            throw new IOException("파일 크기가 너무 큽니다. 최대 5MB까지 업로드 가능합니다.");
        }

        // 파일 확장자 검사
        String originalFilename = imageFile.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IOException("파일명이 유효하지 않습니다.");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IOException("지원하지 않는 파일 형식입니다. " +
                    "허용된 형식: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // 파일 타입 검사 (MIME 타입)
        String contentType = imageFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("이미지 파일만 업로드 가능합니다.");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }

    /**
     * S3에서 실제 파일 삭제 로직
     */
    private void deleteImageFromS3(String imageUrl) {
        log.info("S3에서 이미지 삭제 요청: {}", imageUrl);
        s3UploadService.deleteFile(imageUrl);
    }

    /**
     * 이미지 URL 유효성 검사
     */
    public boolean isValidImageUrl(String imageUrl) {
        return imageUrl != null &&
                !imageUrl.isEmpty() &&
                (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"));
    }
}