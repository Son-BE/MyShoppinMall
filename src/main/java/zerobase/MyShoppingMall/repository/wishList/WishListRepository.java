package zerobase.MyShoppingMall.repository.wishList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.WishList;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    List<WishList> findAllByMemberId(Long memberId);

    Optional<WishList> findByMemberIdAndItemId(Long memberId, Long itemId);

    void deleteByMemberIdAndItemId(Long memberId, Long itemId);

    void deleteAllByMemberId(Long memberId);

    boolean existsByMemberIdAndItemId(Long memberId, Long itemId);

    int countByMemberId(Long memberId);
}
