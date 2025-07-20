package zerobase.MyShoppingMall.controller.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.cart.CartItemResponseDto;
import zerobase.MyShoppingMall.entity.CartItem;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.type.AddToCartResult;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public List<CartItemResponseDto> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        List<CartItem> cartItems = cartService.getCartItems(memberId);

        return cartItems.stream().map(item -> {
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
        }).collect(Collectors.toList());
    }

    @PostMapping("/add")
    public AddToCartResult addItemToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam Long itemId,
                                         @RequestParam int quantity) {
        Long memberId = userDetails.getMember().getId();
        return cartService.addItemToCart(memberId, itemId, quantity);
    }

    @PostMapping("/update")
    public void updateQuantity(@RequestParam Long cartItemId,
                               @RequestParam int quantity) {
        cartService.updateItemQuantity(cartItemId, quantity);
    }

    @PostMapping("/remove")
    public void removeItem(@RequestParam Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
    }

    @PostMapping("/clear")
    public void clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        cartService.clearCart(memberId);
    }

}
