    package zerobase.MyShoppingMall.controller.cart;

    import lombok.RequiredArgsConstructor;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import zerobase.MyShoppingMall.dto.cart.CartAddItemRequestDto;
    import zerobase.MyShoppingMall.dto.cart.CartResponseDto;
    import zerobase.MyShoppingMall.dto.cart.CartUpdateQuantityRequestDto;
    import zerobase.MyShoppingMall.service.cart.CartService;

    @RestController
    @RequestMapping("/api/carts")
    @RequiredArgsConstructor
    public class CartController {
        private final CartService cartService;

        // 회원 장바구니 전체 조회
        @GetMapping("/{cartId}")
        public ResponseEntity<CartResponseDto> getCart(@PathVariable Long cartId) {
            CartResponseDto response = cartService.getCartByCartId(cartId);
            return ResponseEntity.ok(response);
        }

        // 장바구니에 상품 추가
        @PostMapping("/{cartId}/items")
        public ResponseEntity<Void> addItemToCart(
                @PathVariable Long cartId,
                @RequestBody CartAddItemRequestDto requestDto) {
            cartService.addItemToCart(cartId, requestDto.getItemId(), requestDto.getQuantity());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        // 장바구니 내 상품 수량 변경
        @PutMapping("/items/{cartItemId}")
        public ResponseEntity<Void> updateCartItemQuantity(
                @PathVariable Long cartItemId,
                @RequestBody CartUpdateQuantityRequestDto requestDto) {
            cartService.updateCartItemQuantity(cartItemId, requestDto.getNewQuantity());
            return ResponseEntity.ok().build();
        }

        //장바구니 내 아이템 삭제
        @DeleteMapping("/items/{cartItemId}")
        public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
            cartService.deleteCartItem(cartItemId);
            return ResponseEntity.noContent().build();
        }

    }
