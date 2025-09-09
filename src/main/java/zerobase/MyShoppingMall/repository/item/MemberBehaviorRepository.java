package zerobase.MyShoppingMall.repository.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.MemberBehavior;

import java.util.List;

@Repository
public interface MemberBehaviorRepository extends JpaRepository<MemberBehavior, Long> {
    List<MemberBehavior> findByMemberId(Long memberId);
}
