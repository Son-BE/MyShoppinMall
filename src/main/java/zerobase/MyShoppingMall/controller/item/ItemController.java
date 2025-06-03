package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.SortType;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ItemRepository itemRepository;

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<ItemResponseDto> response = itemService.getAllItems();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ItemResponseDto>> getItemsByGender(
            @RequestParam("category") String category) {
        try {
            Gender genderType = Gender.valueOf(category.toUpperCase());
            List<ItemResponseDto> items = itemService.getItemsByGender(genderType);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/latest/gender")
    public ResponseEntity<List<ItemResponseDto>> getLatestItemsByGender(
            @RequestParam("gender") String gender) {
        try {
            Gender genderType = Gender.valueOf(gender.toUpperCase());
            List<ItemResponseDto> items = itemService.getLatestItemsByGender(genderType, 9);
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<ItemResponseDto>> getSortedItemsByGender(
            @RequestParam("gender") String gender,
            @RequestParam("sort") String sort) {

        try {
            Gender genderType = Gender.valueOf(gender.toUpperCase());
            SortType sortType = SortType.valueOf(sort.toUpperCase());

            List<ItemResponseDto> items = itemService.getSortedItemsByGender(genderType, sortType.name());
            return ResponseEntity.ok(items);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of());
        }
    }

    @GetMapping("/detail/{id}")
    public String getItemDetail(@PathVariable Long id, Model model) {
        ItemResponseDto item = itemService.getItem(id);
        model.addAttribute("item", item);
        return "item/detail";
    }

}
