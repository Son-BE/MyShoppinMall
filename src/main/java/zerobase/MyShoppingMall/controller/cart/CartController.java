package zerobase.MyShoppingMall.controller.cart;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.domain.CartItem;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.dto.cart.CartItemResponseDto;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/user/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        List<CartItem> cartItems = cartService.getCartItems(member.getId());
        for(CartItem cartItem : cartItems) {
            log.info("CartItem: itemId={}, name={}, price={}", cartItem.getId());
        }

        List<CartItemResponseDto> response = cartItems.stream().map(item -> {
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

        int totalPrice = response.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", response);
        model.addAttribute("totalPrice", totalPrice);

        return "user/cart";
    }

    @GetMapping("/order")
    public String orderForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        List<CartItem> cartItems = cartService.getCartItems(member.getId());

        List<CartItemResponseDto> response = cartItems.stream().map(item -> {
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

        int totalPrice = response.stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", response);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("memberId", member.getId());

        return "order/input_order";
    }

    @PostMapping("/add")
    public String addItemToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam Long itemId,
                                @RequestParam int quantity) {
        Member member = userDetails.getMember();
        cartService.addItemToCart(member.getId(), itemId, quantity);
        return "redirect:/items";
    }

    @PostMapping("/update")
    public String updateQuantity(@RequestParam Long cartItemId,
                                 @RequestParam int quantity) {
        cartService.updateItemQuantity(cartItemId, quantity);
        return "redirect:/user/cart";
    }

    @PostMapping("/remove")
    public String removeItem(@RequestParam Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return "redirect:/user/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        cartService.clearCart(member.getId());
        return "redirect:/user/cart";
    }

}
