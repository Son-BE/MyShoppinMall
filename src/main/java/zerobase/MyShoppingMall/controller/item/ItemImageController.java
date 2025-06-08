package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.service.item.ItemImageService;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/items/images")
public class ItemImageController {

    private final ItemImageService itemImageService;

    @PostMapping("/{itemId}")
    public ResponseEntity<String> uploadImage(@PathVariable Long itemId,
                                              @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            itemImageService.saveItemImage(itemId, imageFile);
            return ResponseEntity.ok("이미지 업로드 성공");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("이미지 업로드 실패: " + e.getMessage());
        }
    }
}

