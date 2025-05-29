package zerobase.MyShoppingMall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.service.MemberService;

@Controller
public class LoginViewController {
    private MemberService memberService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/mainPage")
    public String mainPage() {
        return "mainpage";
    }

    @PostMapping("/register-form")
    public String registerForm(MemberRequestDto memberRequestDto, RedirectAttributes redirectAttributes) {
        try {
            memberService.registerMember(memberRequestDto);
            return "redirect:/login";  // 회원가입 성공 후 로그인 페이지로 이동
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/signup"; // 회원가입 페이지로 다시 이동
        }
    }

}
