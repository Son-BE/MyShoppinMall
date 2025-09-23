package zerobase.MyShoppingMall.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.Role;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//이메일 기반 조회, 중복확인
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);


    boolean existsByNickName(String nickName);

    List<Member> findByNickNameContaining(String nickName);


    long count();
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByGender(Gender gender);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.role = :role")
    long countByRole(@Param("role") Role role);


}
