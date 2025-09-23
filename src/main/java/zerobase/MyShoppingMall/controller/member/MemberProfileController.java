package zerobase.MyShoppingMall.controller.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.dto.user.MemberUpdateDto;
import zerobase.MyShoppingMall.entity.Address;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.service.member.MemberService;
import zerobase.MyShoppingMall.service.order.AddressService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberProfileController {

    private final MemberService memberService;
    private final AddressService addressService;

    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/items";
        }

        Long memberId = userDetails.getMember().getId();

        MemberResponseDto member = memberService.getMemberProfile(memberId);
        model.addAttribute("member", member);

        Address defaultAddress = addressService.findDefaultAddressByMemberId(memberId);
        model.addAttribute("defaultAddress", defaultAddress);

        return "user/profile_view";
    }



    @GetMapping("/profile/edit")
    public String showUpdateForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        MemberResponseDto member = memberService.getMemberProfile(memberId);

        MemberUpdateDto updateDto = MemberUpdateDto.builder()
                .nickName(member.getNickName())
                .phoneNumber(member.getPhoneNumber())
                .build();

        model.addAttribute("memberUpdateDto", updateDto);
        return "user/profile_edit";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute MemberUpdateDto updateDto,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        Long memberId = userDetails.getMember().getId();
        memberService.updateMemberProfile(memberId, updateDto);
        redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
        return "redirect:/member/profile";
    }


}
