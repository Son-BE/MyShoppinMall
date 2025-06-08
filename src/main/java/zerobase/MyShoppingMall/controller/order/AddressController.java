package zerobase.MyShoppingMall.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.domain.Address;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.member.MemberService;
import zerobase.MyShoppingMall.service.order.AddressService;
import zerobase.MyShoppingMall.service.order.OrderService;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final OrderService orderService;


    @GetMapping("/list")
    public String addressList(
            @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        Member member = userDetails.getMember();
        List<Address> addresses = addressService.getAddressByMember(member);
        model.addAttribute("addresses", addresses);
        return "user/addressList";
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
}
