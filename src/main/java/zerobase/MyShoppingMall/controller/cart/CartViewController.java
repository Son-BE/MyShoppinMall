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
import zerobase.MyShoppingMall.temps.RecommendationResponse;
import zerobase.MyShoppingMall.temps.RecommendationService;
import zerobase.MyShoppingMall.type.AddToCartResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user/cart")
@RequiredArgsConstructor
@Slf4j
public class CartViewController {
    private final CartService cartService;
    private final RecommendationService recommendationService;

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

        addCartPageRecommendations(model, member.getId(), cartItems);

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

        addOrderPageRecommendations(model, member.getId(), cartItems);

        return "order/input_order";
    }

    @PostMapping("/add")
    public String addItemToCartForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @RequestParam Long itemId,
                                    @RequestParam int quantity,
                                    RedirectAttributes redirectAttributes) {
        Long memberId = userDetails.getMember().getId();
        AddToCartResult result = cartService.addItemToCart(memberId, itemId, quantity);

        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
            recordUserInteraction(memberId, itemId, "cart");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
        }

        return "redirect:/";
    }

    private void addCartPageRecommendations(Model model, Long memberId, List<CartItem> cartItems) {
        try {
            if (!cartItems.isEmpty()) {
                List<Integer> cartItemIds = cartItems.stream()
                        .map(item -> item.getItem().getId().intValue())
                        .collect(Collectors.toList());

                Map<String, Object> similarItems = recommendationService.getSimilarItems(
                        cartItemIds, 6
                );
                model.addAttribute("cartSimilarItems", similarItems.get("recommendations"));
            }

            RecommendationResponse personalRecs = recommendationService.getRecommendations(
                    memberId, 8, "hybrid"
            );
            model.addAttribute("personalRecommendations", personalRecs.getRecommendations());

            Map<String, Object> frequentlyBought = recommendationService.getRealTimeRecommendations(
                    memberId, null,
                    cartItems.stream().map(item -> item.getItem().getId().intValue()).collect(Collectors.toList()),
                    4
            );
            model.addAttribute("frequentlyBoughtTogether", frequentlyBought.get("recommendations"));

            log.debug("장바구니 페이지 추천 추가 완료 - 사용자: {}", memberId);

        } catch (Exception e) {
            log.warn("장바구니 페이지 추천 추가 실패 - 사용자: {}", memberId, e);
        }
    }

    /**
     * 주문 페이지에 추천 상품 추가
     */
    private void addOrderPageRecommendations(Model model, Long memberId, List<CartItem> cartItems) {
        try {
            // 1. 마지막 추천 기회 - 유사 상품
            if (!cartItems.isEmpty()) {
                List<Integer> cartItemIds = cartItems.stream()
                        .map(item -> item.getItem().getId().intValue())
                        .collect(Collectors.toList());

                Map<String, Object> lastChanceItems = recommendationService.getSimilarItems(
                        cartItemIds, 4
                );
                model.addAttribute("lastChanceRecommendations", lastChanceItems.get("recommendations"));
            }

            // 2. 추가 구매 유도 상품 (저가격대)
            Map<String, Object> additionalItems = recommendationService.getPopularItems(6);
            model.addAttribute("additionalPurchaseItems", additionalItems.get("recommendations"));

            log.debug("주문 페이지 추천 추가 완료 - 사용자: {}", memberId);

        } catch (Exception e) {
            log.warn("주문 페이지 추천 추가 실패 - 사용자: {}", memberId, e);
        }
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

}
