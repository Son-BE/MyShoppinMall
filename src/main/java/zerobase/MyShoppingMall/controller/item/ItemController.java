package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.ItemCategory;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    //상품 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> createItem(
            @ModelAttribute ItemRequestDto dto,
            @RequestParam("imageFile") MultipartFile imageFile) {

        ItemResponseDto response = itemService.createItem(dto, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //상품 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @ModelAttribute ItemRequestDto dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        ItemResponseDto response = itemService.updateItemWithImage(itemId, dto, imageFile);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long itemId) {
        ItemResponseDto response = itemService.getItem(itemId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<ItemResponseDto> response = itemService.getAllItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category")
    public ResponseEntity<Page<ItemResponseDto>> getItemsByCategory(
            @RequestParam ItemCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ItemResponseDto> items = itemService.getItemsByCategory(category, page, size);
        return ResponseEntity.ok(items);
    }


}
