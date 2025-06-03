package zerobase.MyShoppingMall.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import zerobase.MyShoppingMall.domain.CartItem;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.dto.cart.CartItemResponseDto;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;

import java.util.List;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class CartViewController {
    private final CartService cartService;

    @GetMapping("user/cart")
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        List<CartItem> cartItems = cartService.getCartItems(member.getId());

        List<CartItemResponseDto> cartItemDtos = cartItems.stream().map(item -> {
            String imagePath = null;
            if (item.getItem().getItemImages() != null && !item.getItem().getItemImages().isEmpty()) {
                imagePath = item.getItem().getItemImages().get(0).getImagePath();
            }

            return CartItemResponseDto.builder()
                    .cartItemId(item.getId())
                    .itemId(item.getItem().getId())
                    .itemName(item.getItem().getItemName())
                    .quantity(item.getQuantity())
                    .price(item.getItem().getPrice())
                    .imagePath(imagePath)
                    .build();
        }).toList();

        model.addAttribute("cartItems", cartItemDtos);
        return "user/cart";
    }
}
