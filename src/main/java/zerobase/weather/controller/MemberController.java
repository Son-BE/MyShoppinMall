package zerobase.weather.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zerobase.weather.domain.Member;
import zerobase.weather.dto.user.MemberRequestDto;
import zerobase.weather.dto.user.MemberResponseDto;
import zerobase.weather.repository.MemberRepository;
import zerobase.weather.service.MemberService;

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

}
