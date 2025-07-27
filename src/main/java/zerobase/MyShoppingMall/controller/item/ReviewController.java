package zerobase.MyShoppingMall.controller.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.item.ReviewRequestDto;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.item.ReviewServiceImpl;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/items/{itemId}/reviews")
public class ReviewController {

    private final ReviewServiceImpl reviewService;

    @PostMapping
    public String submitReview(@PathVariable Long itemId,
                               @RequestParam int rating,
                               @RequestParam String content,
//                               @RequestParam Long orderId,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes
                               ) {
        if (userDetails == null) {
            return "redirect:/login"; // 로그인 안 했으면 로그인 페이지로
        }

        Long memberId = userDetails.getMember().getId();

//        boolean canWrite = reviewService.canWriteReview(memberId, itemId, orderId);
//        if (!canWrite) {
//            redirectAttributes.addFlashAttribute("errorMessage", "리뷰 작성 조건을 만족하지 않습니다.");
//            return "redirect:/items/detail/" + itemId;
//        }

        ReviewRequestDto dto = ReviewRequestDto.builder()
                .itemId(itemId)
                .rating(rating)
                .content(content)
//                .orderId(orderId)
                .build();

        reviewService.saveReview(dto, memberId);
        redirectAttributes.addFlashAttribute("successMessage", "리뷰가 등록되었습니다.");

        return "redirect:/items/detail/" + itemId;

    }
}
