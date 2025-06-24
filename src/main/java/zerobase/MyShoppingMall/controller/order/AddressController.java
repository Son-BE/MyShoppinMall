package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.order.AddressSaveRequest;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.entity.OrderAddress;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.order.AddressService;

import java.util.List;

@Controller
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/delete")
    public String deleteAddress(@RequestParam Long addressId,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        addressService.deleteAddress(addressId, member);
        return "redirect:/address/list";
    }

    @PostMapping("/create")
    public String createAddress(@ModelAttribute OrderAddress orderAddress,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        return "redirect:/address/list";
    }

    @PostMapping("/update")
    public String updateAddress(@RequestParam Long id,
                                @ModelAttribute OrderAddress orderAddress,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        return "redirect:/address/list";
    }

    @GetMapping("/manage")
    public String showAddressForm(Model model) {
        model.addAttribute("address", new AddressSaveRequest());
        return "user/address/manage";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute AddressSaveRequest request,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        addressService.saveDefaultAddress(request, userDetails.getMember().getId());
        return "redirect:/members/profile";
    }

    @PostMapping("/{id}/default")
    public String setDefaultAddress(@PathVariable("id") Long addressId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getMember();
        addressService.setDefaultAddress(member, addressId);
        return "redirect:/address/list";
    }

}
