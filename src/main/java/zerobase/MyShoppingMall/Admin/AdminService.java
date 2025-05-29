package zerobase.MyShoppingMall.Admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    public long getTotalMemberCount() {
        return memberRepository.count();
    }
}
