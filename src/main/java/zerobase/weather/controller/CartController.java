//package zerobase.weather.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import zerobase.weather.dto.cart.CartItemAddRequestDTO;
//import zerobase.weather.dto.cart.CartItemResponseDTO;
//import zerobase.weather.service.CartService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/cart")
//@RequiredArgsConstructor
//public class CartController {
//
//    private final CartService cartService;
//
//    //memberId를 매개변수로 받고, 실제 서비스에서는 로그인 사용자 기반 처리 예정
//    @PostMapping("/add")
//    public ResponseEntity<Void> addToCart(@RequestBody CartItemAddRequestDTO requestDTO,
//                                          @RequestParam Long memberId) {
//        cartService.addToCart(requestDTO, memberId);
//        return ResponseEntity.ok().build();
//    }
//
//    @GetMapping
//    public ResponseEntity<List<CartItemResponseDTO>> getCartItems(@RequestParam Long memberId) {
//        return ResponseEntity.ok(cartService.getCartItems(memberId));
//    }
//
//    @DeleteMapping("/remove")
//    public ResponseEntity<Void> removeFromCart(@RequestParam Long memberId,
//                                               @RequestParam Long cartItemId) {
//        cartService.removeFromCart(memberId, cartItemId);
//        return ResponseEntity.ok().build();
//    }
//
//}
