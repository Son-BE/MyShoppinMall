package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.item.ItemRequestDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemViewController {
    private final ItemService itemService;

    @PostMapping("/new")
    public String createItem(@ModelAttribute ItemRequestDto dto) {
        itemService.createItem(dto);
        return "redirect:/dashboard"; // 등록 후 이동
    }

    @GetMapping("/items")
    public String viewItemList(@RequestParam(required = false) ItemCategory category,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model)  {
        if(category != null) {
            model.addAttribute("items", itemService.getItemsByCategory(category, page, size));
        } else {
            model.addAttribute("items", itemService.getAllItems());
        }
        return "item-list";
    }
}
