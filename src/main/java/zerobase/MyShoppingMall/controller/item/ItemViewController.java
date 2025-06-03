package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.item.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemViewController {
    private final ItemService itemService;

    @GetMapping("/detail/{id}")
    public String getItemDetail(@PathVariable Long id, Model model) {
        ItemResponseDto item = itemService.getItem(id);
        model.addAttribute("item", item);
        return "user/detail";
    }
}
