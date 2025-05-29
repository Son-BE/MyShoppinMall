package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.ItemService;
import zerobase.MyShoppingMall.type.Category;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/items")
public class AdminItemController {

    private final ItemService itemService;

    @GetMapping
    public String list(@RequestParam(required = false) Category category,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<ItemResponseDto> items = (category != null)
                ? itemService.getItemsByCategory(category, page, size)
                : itemService.getAllItemsPageable(page, size);

        model.addAttribute("items", items.getContent());
        return "admin/item/list";
    }
}
