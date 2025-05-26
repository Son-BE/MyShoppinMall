package zerobase.MyShoppingMall.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.user.MemberRequestDto;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.repository.MemberRepository;
import zerobase.MyShoppingMall.service.MemberService;

@RestController
@RequestMapping("api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberResponseDto> register(@RequestBody MemberRequestDto memberRequestDto) {
        MemberResponseDto responseDto = memberService.registerMember(memberRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/email")
    public ResponseEntity<MemberResponseDto> getByEmail(@RequestParam String email) {
        return memberService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> getById(@PathVariable Long id) {
        return memberService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        memberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }




}
