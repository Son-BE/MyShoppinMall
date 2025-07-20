package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import zerobase.MyShoppingMall.service.member.MemberService;
import zerobase.MyShoppingMall.type.Gender;

import java.time.LocalDateTime;
//import zerobase.MyShoppingMall.service.order.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalMembers", memberService.getTotalMemberCount());
        model.addAttribute("newMembers", memberService.getNewMemberCountInDays(7));
        model.addAttribute("maleMembers", memberService.getMemberCountByGender(Gender.MALE));
        model.addAttribute("femaleMembers", memberService.getMemberCountByGender(Gender.FEMALE));

        //최근 1개월 가입 수
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        model.addAttribute("monthlyNewMembers", memberService.getMemberCountInRange(oneMonthAgo, now));


        return "/dashboard";
    }
}
