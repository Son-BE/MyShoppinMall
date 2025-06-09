package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.domain.Address;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.domain.OrderAddress;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.AddressService;

import java.util.List;

@Controller
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
//    private final OrderService orderService;


    @GetMapping("/list")
    public String addressList(
            @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        List<Address> addresses = addressService.getAddressByMember(member);
        model.addAttribute("addresses", addresses);
        return "user/addressList";
    }
    @GetMapping("/create")
    public String createAddressForm() {
        return "user/create_Address";
    }

    @PostMapping("/delete")
    public String deleteAddress(@RequestParam Long addressId,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        addressService.deleteAddress(addressId, member);
        return "redirect:/address/list";
    }

    @PostMapping("/edit/{id}")
    public String editAddress(@PathVariable Long id, Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        Address address = addressService.getAddressByIdAndMember(id, member);
        model.addAttribute("address", address);
        return "user/editAddress";
    }

    @PostMapping("/create")
    public String createAddress(@ModelAttribute OrderAddress orderAddress,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
//        addressService.createAddress(orderAddress, member);
        return "redirect:/address/list";
    }

    @PostMapping("/update")
    public String updateAddress(@RequestParam Long id,
                                @ModelAttribute OrderAddress orderAddress,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
//        addressService.updateAddress(id, orderAddress, member);
        return "redirect:/address/list";
    }

}
