package zerobase.MyShoppingMall.controller.member;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.service.member.MemberService;

@Controller
@RequiredArgsConstructor
public class LoginViewController {
    private final MemberService memberService;

    @GetMapping("/")
    public String initPage() {
        return "login";
    }

//    @GetMapping("/login")
//    public String loginPage() {
//        return "login";
//    }
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            model.addAttribute("_csrf", csrfToken);
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

//    @GetMapping("/mainPage")
//    public String mainPage() {
//        return "mainPage";
//    }

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
