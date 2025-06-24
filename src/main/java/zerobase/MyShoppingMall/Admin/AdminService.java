package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    @Transactional
    public void grantPoints(Long memberId, Long points) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Long currentPoints = member.getPoint();
        if (currentPoints == null) {
            currentPoints = 0L;
        }

        member.setPoint(currentPoints + points);
        memberRepository.save(member);
    }

}
