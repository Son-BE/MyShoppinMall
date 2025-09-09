package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/items")
public class AdminItemController {

    private final ItemService itemService;

    @PostMapping("/create")
    public String createItem(@ModelAttribute ItemRequestDto requestDto) throws IOException {
        itemService.createItem(requestDto);
        return "redirect:/dashboard";
    }

    // 상품 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> deleteItem(
            @PathVariable Long itemId) {
        log.info("삭제요청 - itemId : {}", itemId);
        itemService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    //상품 수정
    @PutMapping(path = "/{itemId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @ModelAttribute ItemRequestDto dto,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) throws IOException {

        log.info("▶▶▶ updateItem() 호출, itemId={}", itemId);
        log.info("▶▶▶ 받은 데이터: {}", dto);

        ItemResponseDto response = itemService.updateItemWithImage(itemId, dto, imageFile);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view")
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<ItemResponseDto> response = itemService.getAllItems();
        return ResponseEntity.ok(response);
    }

    // 상품 목록 페이징
    @GetMapping
    public String list(@RequestParam(required = false) ItemCategory category,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<ItemResponseDto> items = (category != null)
                ? itemService.getItemsByCategory(category, page, size)
                : itemService.getAllItemsPageable(page, size);

        model.addAttribute("items", items.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", items.getTotalPages() == 0 ? 1 : items.getTotalPages());
        model.addAttribute("category", category);

        return "admin/item/list";
    }


    // 상품 등록 폼 이동
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("item", new ItemRequestDto());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("subCategories", new HashMap<String, List<ItemSubCategory>>());
        return "admin/item/create";
    }


    // 상품 수정 폼 이동
    @GetMapping("/edit/{itemId}")
    public String showEditForm(@PathVariable Long itemId, Model model) {
        ItemResponseDto item = itemService.getItem(itemId);
        model.addAttribute("item", item);
        model.addAttribute("categories", ItemCategory.values());
        return "admin/item/edit";
    }

    @GetMapping("/get-subcategories")
    @ResponseBody
    public Map<String, List<ItemSubCategory>> getSubCategoriesByCategory(@RequestParam ItemCategory category) {
        Map<String, List<ItemSubCategory>> subCategoryMap = Arrays.stream(ItemSubCategory.values())
                .filter(sub -> sub.getItemCategory() == category)
                .collect(Collectors.groupingBy(
                        sub -> sub.getItemCategory().name(),
                        Collectors.toList()
                ));

        return subCategoryMap;
    }
}
