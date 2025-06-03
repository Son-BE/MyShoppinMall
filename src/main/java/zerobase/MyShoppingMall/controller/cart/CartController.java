package zerobase.MyShoppingMall.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.domain.CartItem;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.dto.cart.CartItemResponseDto;
import zerobase.MyShoppingMall.service.cart.CartService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    // 로그인 회원의 장바구니 목록 조회
    @GetMapping
    public ResponseEntity<List<CartItemResponseDto>> getCartItems(@AuthenticationPrincipal zerobase.MyShoppingMall.service.member.CustomUserDetails userDetails) {
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

        return ResponseEntity.ok(response);
    }

    // 장바구니에 아이템 추가
    @PostMapping("/add")
    public ResponseEntity<String> addItemToCart(@AuthenticationPrincipal zerobase.MyShoppingMall.service.member.CustomUserDetails userDetails,
                                                @RequestParam Long itemId,
                                                @RequestParam int quantity) {
        Member member = userDetails.getMember();
        cartService.addItemToCart(member.getId(), itemId, quantity);
        return ResponseEntity.ok("장바구니에 추가되었습니다.");
    }

    // 장바구니 아이템 수량 변경
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<String> updateQuantity(@PathVariable Long cartItemId,
                                                 @RequestParam int quantity) {
        cartService.updateItemQuantity(cartItemId, quantity);
        return ResponseEntity.ok("수량이 변경되었습니다.");
    }

    // 장바구니 아이템 삭제
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeItem(@PathVariable Long cartItemId) {
        cartService.clearCart(cartItemId);
        return ResponseEntity.ok("장바구니 아이템이 삭제되었습니다.");
    }

    // 장바구니 비우기
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@AuthenticationPrincipal zerobase.MyShoppingMall.service.member.CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        cartService.clearCart(member.getId());
        return ResponseEntity.ok("장바구니가 비워졌습니다.");
    }
}
