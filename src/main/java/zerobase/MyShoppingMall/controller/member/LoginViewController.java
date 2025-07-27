package zerobase.MyShoppingMall.controller.member;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.item.ItemResponseDto;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.service.item.ItemService;
import zerobase.MyShoppingMall.service.member.MemberService;
import zerobase.MyShoppingMall.type.Gender;

@Controller
@RequiredArgsConstructor
public class LoginViewController {
    private final MemberService memberService;
    private final ItemService itemService;

    @GetMapping("/")
    public String initPage(
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "sort", required = false, defaultValue = "latest") String sort,
            @RequestParam(value = "category", required = false) String subCategory,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            Model model) {

        Gender genderEnum = null;
        if (gender != null && !gender.isEmpty()) {
            try {
                genderEnum = Gender.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                genderEnum = null;
            }
        }

        Page<ItemResponseDto> itemPage = itemService.findItems(genderEnum, sort, subCategory, page, 16);

        model.addAttribute("items", itemPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemPage.getTotalPages());
        model.addAttribute("selectedGender", gender);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedCategory", subCategory);

        return "mainPage";
    }


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
