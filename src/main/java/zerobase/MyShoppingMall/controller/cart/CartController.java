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
import zerobase.MyShoppingMall.service.item.S3UploadService;
import zerobase.MyShoppingMall.temps.RecommendationService;
import zerobase.MyShoppingMall.type.AddToCartResult;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    private final RecommendationService recommendationService;

    @GetMapping
    public List<CartItemResponseDto> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        List<CartItem> cartItems = cartService.getCartItems(memberId);

        return cartItems.stream().map(item -> {
            return CartItemResponseDto.builder()
                    .cartItemId(item.getId())
                    .itemId(item.getItem().getId())
                    .itemName(item.getItem().getItemName())
                    .quantity(item.getQuantity())
                    .price(item.getItem().getPrice())
                    .build();
        }).collect(Collectors.toList());
    }

    @PostMapping("/add")
    public AddToCartResult addItemToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam Long itemId,
                                         @RequestParam int quantity) {
        Long memberId = userDetails.getMember().getId();

        // 장바구니 추가 실행
        AddToCartResult result = cartService.addItemToCart(memberId, itemId, quantity);

        // 추천 시스템: 장바구니 추가 상호작용 기록
        if (result.isSuccess()) {
            recordUserInteraction(memberId, itemId, "cart");
        }

        return result;
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

    /**
     * 사용자 상호작용 기록 (안전하게 처리)
     */
    private void recordUserInteraction(Long memberId, Long itemId, String action) {
        try {
            recommendationService.recordUserInteraction(memberId, itemId, action);
            log.debug("장바구니 상호작용 기록 - 사용자: {}, 상품: {}, 액션: {}", memberId, itemId, action);
        } catch (Exception e) {
            log.warn("장바구니 상호작용 기록 실패 - 사용자: {}, 상품: {}, 액션: {}",
                    memberId, itemId, action, e);
        }
    }

//    @GetMapping
//    public List<CartItemResponseDto> getCartItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        Long memberId = userDetails.getMember().getId();
//        List<CartItem> cartItems = cartService.getCartItems(memberId);
//
//        return cartItems.stream().map(item -> {
//
//            return CartItemResponseDto.builder()
//                    .cartItemId(item.getId())
//                    .itemId(item.getItem().getId())
//                    .itemName(item.getItem().getItemName())
//                    .quantity(item.getQuantity())
//                    .price(item.getItem().getPrice())
//                    .build();
//        }).collect(Collectors.toList());
//    }
//
//    @PostMapping("/add")
//    public AddToCartResult addItemToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
//                                         @RequestParam Long itemId,
//                                         @RequestParam int quantity) {
//        Long memberId = userDetails.getMember().getId();
//        return cartService.addItemToCart(memberId, itemId, quantity);
//    }
//
//    @PostMapping("/update")
//    public void updateQuantity(@RequestParam Long cartItemId,
//                               @RequestParam int quantity) {
//        cartService.updateItemQuantity(cartItemId, quantity);
//    }
//
//    @PostMapping("/remove")
//    public void removeItem(@RequestParam Long cartItemId) {
//        cartService.deleteCartItem(cartItemId);
//    }
//
//    @PostMapping("/clear")
//    public void clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        Long memberId = userDetails.getMember().getId();
//        cartService.clearCart(memberId);
//    }

}
