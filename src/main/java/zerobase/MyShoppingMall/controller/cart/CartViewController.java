package zerobase.MyShoppingMall.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.cart.CartItemResponseDto;
import zerobase.MyShoppingMall.entity.CartItem;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.cart.CartService;
import zerobase.MyShoppingMall.type.AddToCartResult;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/cart")
@RequiredArgsConstructor
public class CartViewController {
    private final CartService cartService;

    @GetMapping
    public String getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        List<CartItem> cartItems = cartService.getCartItems(member.getId());

        List<CartItemResponseDto> response = cartItems.stream().map(item -> {
            String imagePath = null;
            if (item.getItem().getImageUrl() != null && !item.getItem().getImageUrl().isEmpty()) {
                imagePath = item.getItem().getImageUrl();  // S3 URL 사용
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

            if (item.getItem().getImageUrl() != null && !item.getItem().getImageUrl().isEmpty()) {
                imagePath = item.getItem().getImageUrl();  // S3 URL 사용
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
    public String addItemToCartForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam Long itemId,
                                    @RequestParam int quantity, RedirectAttributes redirectAttributes) {
        Long memberId = userDetails.getMember().getId();
        AddToCartResult result = cartService.addItemToCart(memberId, itemId, quantity);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }


        return "redirect:/detail/" + itemId;
    }
}
