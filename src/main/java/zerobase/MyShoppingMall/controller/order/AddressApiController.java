package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.MyShoppingMall.dto.order.AddressDto;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.AddressService;

@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressApiController {

    private final AddressService addressService;

    @GetMapping("/default")
    public ResponseEntity<AddressDto> getDefaultAddress(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        Address defaultAddress = addressService.findDefaultAddressByMemberId(memberId);
        if (defaultAddress == null) {
            return ResponseEntity.noContent().build();
        }

        AddressDto dto = new AddressDto(
                defaultAddress.getPostalCode(),
                defaultAddress.getAddressLine1(),
                defaultAddress.getAddressLine2()
        );
        return ResponseEntity.ok(dto);
    }
}
