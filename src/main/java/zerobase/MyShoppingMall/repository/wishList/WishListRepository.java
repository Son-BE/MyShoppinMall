package zerobase.MyShoppingMall.repository.wishList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.WishList;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    List<WishList> findAllByMemberId(Long memberId);

    Optional<WishList> findByMemberIdAndItemId(Long memberId, Long itemId);

    @Modifying
    @Query("DELETE FROM WishList w WHERE w.member.id = :memberId AND w.item.id = :itemId")
    int deleteByMemberIdAndItemId(@Param("memberId") Long memberId, @Param("itemId") Long itemId);

    @Modifying
    @Query("DELETE FROM WishList w WHERE w.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

    boolean existsByMemberIdAndItemId(Long memberId, Long itemId);

    int countByMemberId(Long memberId);
    int countByItemId(Long itemId);

    @Modifying
    @Query("DELETE FROM WishList w WHERE w.item.id = :itemId")
    void deleteByItemId(@Param("itemId") Long itemId);


}
