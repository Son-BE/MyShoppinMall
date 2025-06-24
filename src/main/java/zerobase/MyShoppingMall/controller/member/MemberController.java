package zerobase.MyShoppingMall.controller.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.dto.user.MemberUpdateDto;
import zerobase.MyShoppingMall.service.member.CustomUserDetails;
import zerobase.MyShoppingMall.service.member.MemberService;

import java.util.Map;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
//    private final JwtTokenProvider jwtTokenProvider;

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

    //프로필 정보
    @GetMapping("/profile")
    public ResponseEntity<MemberResponseDto> getProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 인증 실패
        }
        MemberResponseDto dto = memberService.getProfile(userDetails.getMember().getId());
        return ResponseEntity.ok(dto);
    }

    //프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                           @RequestBody MemberUpdateDto dto) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            memberService.updateMemberProfile(userDetails.getMember().getId(), dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //포인트 불러오기
    @GetMapping("/point")
    public ResponseEntity<Long> getPoint(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long point = userDetails.getMember().getPoint();
        return ResponseEntity.ok(point);
    }




}
