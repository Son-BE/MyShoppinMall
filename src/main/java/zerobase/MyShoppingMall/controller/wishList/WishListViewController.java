package zerobase.MyShoppingMall.controller.wishList;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import zerobase.MyShoppingMall.dto.wishlist.WishListDto;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.wishList.WishListService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WishListViewController {
    private final WishListService wishListService;

    @GetMapping("user/wishList")
    public String viewWishList(@AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        Long memberId = userDetails.getMember().getId();
        List<WishListDto> wishList = wishListService.getWishListByMember(memberId);
        model.addAttribute("wishList", wishList);
        return "user/wishList";
    }
}
