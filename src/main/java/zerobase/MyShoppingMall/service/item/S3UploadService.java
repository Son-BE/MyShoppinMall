package zerobase.MyShoppingMall.service.item;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // UUID 기반으로 파일명 생성 (중복 방지)
        String fileName = UUID.randomUUID().toString() + extension;

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(multipartFile.getContentType());

        // S3에 업로드
        s3.putObject(bucket, fileName, multipartFile.getInputStream(), metadata);

        // 접근 가능한 URL 반환
        return s3.getUrl(bucket, fileName).toString();
    }

    public void deleteFile(String imageUrl) {
        String fileName = extractFileNameFromUrl(imageUrl);
        if (s3.doesObjectExist(bucket, fileName)) {
            s3.deleteObject(bucket, fileName);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractFileNameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }
}
