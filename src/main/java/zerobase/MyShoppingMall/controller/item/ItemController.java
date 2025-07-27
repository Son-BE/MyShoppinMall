package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.entity.Review;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.repository.item.ReviewRepository;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.service.item.ReviewServiceImpl;
import zerobase.MyShoppingMall.type.Gender;

import java.util.List;


@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;
    private final ReviewServiceImpl reviewService;
    private final ReviewRepository reviewRepository;

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
                genderEnum = null;
            }
        }

        Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, subCategory, page, 16);

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedCategory", subCategory);

        return "mainPage";
    }

    @GetMapping("/detail/{id}")
    public String getItemDetail(@PathVariable Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        ItemResponseDto item = itemService.getItemWithCache(id);
        List<Review> reviews = reviewRepository.findByItemIdOrderByCreatedAtDesc(id);


        boolean canWriteReview = true;
        if (userDetails != null) {
            Long memberId = userDetails.getMember().getId();
            canWriteReview = !reviewRepository.existsByItemIdAndMemberId(id, memberId);
        }

        model.addAttribute("item", item);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("canWriteReview", canWriteReview);

        return "user/detail";
    }

}
