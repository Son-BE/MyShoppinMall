package zerobase.MyShoppingMall.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.dto.user.MemberResponseDto;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.repository.member.MemberRepository;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserCacheService implements MemberCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final MemberRepository memberRepository;
    private static final String MEMBER_KEY = "member";


    @Override
    public MemberResponseDto getMemberById(Long memberId) {
        String key = MEMBER_KEY + memberId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (MemberResponseDto) cached;
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new RuntimeException("Member not found"));
        MemberResponseDto responseDto = MemberResponseDto.fromEntity(member);

        redisTemplate.opsForValue().set(key, responseDto, Duration.ofMinutes(30));
        return responseDto;
    }


    @Override
    public void putMember(MemberResponseDto member) {
        String key = MEMBER_KEY + member.getId();
        redisTemplate.opsForValue().set(key, member, Duration.ofMinutes(30));
    }

    @Override
    public void evictMember(Long memberId) {
        redisTemplate.delete(MEMBER_KEY + memberId);
    }
}
