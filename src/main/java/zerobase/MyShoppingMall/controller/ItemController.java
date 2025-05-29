package zerobase.MyShoppingMall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.ItemService;
import zerobase.MyShoppingMall.type.Category;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    //상품 등록
    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody ItemRequestDto dto) {
        ItemResponseDto response = itemService.createItem(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //상품 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long itemId,
                                                      @RequestBody ItemRequestDto dto) {
        ItemResponseDto response = itemService.updateItem(itemId, dto);
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
            @RequestParam Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<ItemResponseDto> items = itemService.getItemsByCategory(category, page, size);
        return ResponseEntity.ok(items);
    }


}
