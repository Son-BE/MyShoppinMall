package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long memberCount = adminService.getTotalMemberCount();
        model.addAttribute("memberCount", memberCount);
        return "admin/dashboard";
    }
}
