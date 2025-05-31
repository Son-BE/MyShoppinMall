package zerobase.MyShoppingMall.controller.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.service.member.MemberService;

import java.util.Map;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody MemberRequestDto memberRequestDto) {
        try {
            MemberResponseDto responseDto = memberService.registerMember(memberRequestDto);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());
        return "redirect:/login";
    }

    @PostMapping("/register-form")
    public ResponseEntity<?> registerForm(MemberRequestDto memberRequestDto) {
        try {
            MemberResponseDto responseDto = memberService.registerMember(memberRequestDto);
            return ResponseEntity.ok(responseDto);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //이메일로 회원 조회
    @GetMapping("/by-email")
    public ResponseEntity<MemberResponseDto> getByEmail(@RequestParam String email) {
        return memberService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 이메일 중복 여부 확인
    @GetMapping("/exists-email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(memberService.existsByEmail(email));
    }

    //닉네임 중복 여부 확인
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickName) {
        boolean exists = memberService.existsByNickName(nickName);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ID로 회원 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> getById(@PathVariable Long id) {
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 회원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        memberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    //회원 탈퇴

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        memberService.deleteByEmail(email);
        new SecurityContextLogoutHandler().logout(
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest(),
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse(),
                SecurityContextHolder.getContext().getAuthentication()
        );
        return ResponseEntity.noContent().build();
    }


}
