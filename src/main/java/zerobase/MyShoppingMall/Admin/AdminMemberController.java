package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/members")
public class AdminMemberController {

    private final MemberRepository memberRepository;

    public String getMembers(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<Member> membersPage = memberRepository.findAll(pageable);

        model.addAttribute("membersPage", membersPage);
        model.addAttribute("members", membersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", membersPage.getTotalPages());

        return "admin/members/member_list";
    }
}