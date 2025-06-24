package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.dto.item.ItemPointGrantRequest;
import zerobase.MyShoppingMall.repository.member.MemberRepository;


import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/points")
public class AdminPointController {

    private final AdminService adminService;
    private final MemberRepository memberRepository;

    @GetMapping("/grant")
    public String grantPointForm(@RequestParam(required = false) String keyword, Model model) {
        List<Member> members;
        if (keyword != null && !keyword.isBlank()) {
            members = memberRepository.findByNickNameContaining(keyword);
        } else {
            members = memberRepository.findAll();
        }
        model.addAttribute("members", members);
        model.addAttribute("keyword", keyword);
        return "admin/points/grant_form";
    }


    @PostMapping("/grant")
    public String grantPoint(@ModelAttribute ItemPointGrantRequest request, Model model) {
        try {
            adminService.grantPoints(request.getMemberId(), request.getPoints());
            model.addAttribute("message", "포인트가 정상적으로 지급되었습니다.");
        } catch (Exception e) {
            model.addAttribute("message", "포인트 지급 중 오류가 발생했습니다: " + e.getMessage());
        }
        return "admin/points/grant_form";
    }
}
