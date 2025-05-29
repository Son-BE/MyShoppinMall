package zerobase.MyShoppingMall.controller;

import jakarta.persistence.Column;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zerobase.MyShoppingMall.repository.ItemRepository;
import zerobase.MyShoppingMall.service.ItemService;
import zerobase.MyShoppingMall.type.Category;

@Controller
@RequiredArgsConstructor
public class ItemViewController {
    private final ItemService itemService;

    @GetMapping("/items")
    public String viewItemList(@RequestParam(required = false) Category category,
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
