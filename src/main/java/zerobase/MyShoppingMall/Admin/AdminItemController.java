package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/items")
public class AdminItemController {

    private final ItemService itemService;

    // 상품 목록 보기
    @GetMapping
    public String list(@RequestParam(required = false) ItemCategory category,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<ItemResponseDto> items = (category != null)
                ? itemService.getItemsByCategory(category, page, size)
                : itemService.getAllItemsPageable(page, size);

        model.addAttribute("items", items.getContent());
        return "admin/item/list";
    }

    // 상품 등록 폼 이동
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("item", new ItemRequestDto());
        model.addAttribute("categories", ItemCategory.values());
        model.addAttribute("subCategories", ItemSubCategory.values());
        return "admin/item/create";
    }

    // 상품 등록 처리
    @PostMapping("/create")
    public String createItem(@ModelAttribute ItemRequestDto itemRequestDto,
                             @RequestParam("imageFile") MultipartFile imageFile) {
        itemService.createItem(itemRequestDto,imageFile);
        return "redirect:/admin/items";
    }

    // 상품 수정 폼 이동
    @GetMapping("/edit/{itemId}")
    public String showEditForm(@PathVariable Long itemId, Model model) {
        ItemResponseDto item = itemService.getItem(itemId);
        model.addAttribute("item", item);
        model.addAttribute("categories", ItemCategory.values());
        return "admin/item/edit";
    }
}
