package zerobase.MyShoppingMall.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.dto.user.MemberBehaviorRequestDto;
import zerobase.MyShoppingMall.entity.MemberBehavior;
import zerobase.MyShoppingMall.repository.item.MemberBehaviorRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.type.BehaviorType;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberBehaviorService {
    private final MemberRepository memberRepository;
    private final MemberBehaviorRepository memberBehaviorRepository;

    public void recordBehavior(MemberBehaviorRequestDto dto) {
        MemberBehavior behavior = MemberBehavior.builder()
                .memberId(dto.getMemberId())
                .itemId(dto.getItemId())
                .behaviorType(dto.getBehaviorType())
                .timestamp(LocalDateTime.now())
                .build();

        memberBehaviorRepository.save(behavior);
    }
}
