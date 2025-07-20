package zerobase.MyShoppingMall.redis;

import zerobase.MyShoppingMall.dto.user.MemberResponseDto;

public interface MemberCacheService {
    MemberResponseDto getMemberById(Long memberId);
    void putMember(MemberResponseDto member);
    void evictMember(Long memberId);
}
