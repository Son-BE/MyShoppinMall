package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.service.item.S3UploadService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ImageController {
    private final S3UploadService s3UploadService;

   @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
       return s3UploadService.uploadFile(file);
   }
}
