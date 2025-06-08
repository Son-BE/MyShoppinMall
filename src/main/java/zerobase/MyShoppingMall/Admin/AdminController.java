package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import zerobase.MyShoppingMall.service.member.MemberService;
//import zerobase.MyShoppingMall.service.order.OrderService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final MemberService memberService;
//    private final OrderService orderService;


    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long memberCount = memberService.countMembers();
//        long orderCount = orderService.getTotalOrderCount();
        model.addAttribute("memberCount", memberCount);
//        model.addAttribute("orderCount", orderCount);
        return "/dashboard";
    }
}
