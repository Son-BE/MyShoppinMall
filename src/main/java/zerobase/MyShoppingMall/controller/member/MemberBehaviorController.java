package zerobase.MyShoppingMall.controller.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zerobase.MyShoppingMall.service.member.MemberBehaviorService;

@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class MemberBehaviorController {

    private final MemberBehaviorService memberBehaviorService;


}
