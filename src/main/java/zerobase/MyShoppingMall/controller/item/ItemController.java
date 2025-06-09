package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemSubCategory;

import java.util.List;


@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public String getItemsPage(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "category", required = false) String subCategory,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            Model model) {

        Gender genderEnum = null;
        if (gender != null && !gender.isEmpty()) {
            try {
                genderEnum = Gender.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                genderEnum = null;  // 기본값 또는 무시
            }
        }

        Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, subCategory, page, 16);

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedCategory", subCategory);

        return "/mainPage";
    }

    @GetMapping("/detail/{id}")
    public String getItemDetail(@PathVariable Long id, Model model) {
        ItemResponseDto item = itemService.getItem(id);
        model.addAttribute("item", item);
        return "/user/detail";
    }

//    @GetMapping("/filter")
//    public String filterBySubCategory(@RequestParam("subcategory") ItemSubCategory subCategory, Model model) {
//        List<ItemResponseDto> items = itemService.findBy(subCategory);
//        model.addAttribute("items", items);
//        return "/mainPage";
//    }
}
