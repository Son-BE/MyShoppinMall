//package zerobase.MyShoppingMall.repository.member;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import zerobase.MyShoppingMall.entity.SocialMember;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface SocialMemberRepository extends JpaRepository<SocialMember, Long> {
//    Optional<SocialMember> findByEmail(String email);
//    boolean existsByEmail(String email);
//    Long countByEmail(String email);
//    boolean existsBySocialMemberPhone(String phone);
//    Optional<SocialMember> findBySocialMemberName(String name);
//
//}
