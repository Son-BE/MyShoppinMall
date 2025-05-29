package zerobase.MyShoppingMall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.domain.Member;

import java.util.Optional;

//이메일 기반 조회, 중복확인
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    long count();

    boolean existsByNickName(String nickName);
}
