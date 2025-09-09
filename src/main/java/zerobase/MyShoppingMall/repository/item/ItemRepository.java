package zerobase.MyShoppingMall.repository.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;
import zerobase.MyShoppingMall.type.StyleTag;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Override
    Optional<Item> findById(Long itemId);

    //필터링
    Page<Item> findByCategory(ItemCategory category, Pageable pageable);
    Page<Item> findByGenderAndSubCategory(Gender gender, ItemSubCategory subCategory, Pageable pageable);
    Page<Item> findBySubCategory(ItemSubCategory subCategory, Pageable pageable);
    Page<Item> findByGender(Gender gender, Pageable pageable);
    List<Item> findAll();

    /**
     * 아이템 조회수 증가
     */
    @Modifying
    @Query("UPDATE Item i SET i.viewCount = i.viewCount + 1 WHERE i.id = :itemId")
    void incrementViewCount(@Param("itemId") Long itemId);

    /**
     * 아이템 카트 추가수 증가
     */
    @Modifying
    @Query("UPDATE Item i SET i.cartCount = i.cartCount + 1 WHERE i.id = :itemId")
    void incrementCartCount(@Param("itemId") Long itemId);

    /**
     * 아이템 주문수 증가
     */
    @Modifying
    @Query("UPDATE Item i SET i.orderCount = i.orderCount + 1 WHERE i.id = :itemId")
    void incrementOrderCount(@Param("itemId") Long itemId);

    /**
     * 아이템 찜 카운트 증가
     */
    @Modifying
    @Query("UPDATE Item i SET i.wishCount = i.wishCount + 1 WHERE i.id = :itemId")
    void incrementWishCount(@Param("itemId") Long itemId);

    /**
     * 아이템 찜 카운트 감소 (0 이하로 내려가지 않도록 보호)
     */
    @Modifying
    @Query("UPDATE Item i SET i.wishCount = CASE WHEN i.wishCount > 0 THEN i.wishCount - 1 ELSE 0 END WHERE i.id = :itemId")
    void decrementWishCount(@Param("itemId") Long itemId);

    /**
     * 아이템의 모든 카운트를 0으로 초기화
     */
    @Modifying
    @Query("UPDATE Item i SET i.viewCount = 0, i.cartCount = 0, i.orderCount = 0, i.wishCount = 0 WHERE i.id = :itemId")
    void resetItemCounts(@Param("itemId") Long itemId);

    // 추가 유용한 메서드들

    /**
     * 찜 수가 높은 순으로 아이템 조회
     */
    @Query("SELECT i FROM Item i WHERE i.deleteType = 'N' ORDER BY i.wishCount DESC")
    Page<Item> findAllByOrderByWishCountDesc(Pageable pageable);

    /**
     * 특정 아이템을 제외하고 찜 수가 높은 아이템 조회 (추천용)
     */
    @Query("SELECT i FROM Item i WHERE i.deleteType = 'N' AND i.id != :excludeItemId ORDER BY i.wishCount DESC")
    List<Item> findTop10ByIdNotOrderByWishCountDesc(@Param("excludeItemId") Long excludeItemId, Pageable pageable);

    /**
     * 카테고리별 인기 아이템 조회
     */
    @Query("SELECT i FROM Item i WHERE i.deleteType = 'N' AND i.category = :category ORDER BY i.wishCount DESC, i.viewCount DESC")
    List<Item> findPopularItemsByCategory(@Param("category") ItemCategory category, Pageable pageable);


    @Query("SELECT COUNT(i) FROM Item i WHERE i.gender = :gender")
    long countByGender(@Param("gender") Gender gender);

    @Query("SELECT AVG(i.price) FROM Item i")
    Double findAveragePrice();

    @Query("SELECT MIN(i.price) FROM Item i")
    Integer findMinPrice();

    @Query("SELECT MAX(i.price) FROM Item i")
    Integer findMaxPrice();

    long deleteByItemNameContaining(String 테스트);

    @Query("SELECT i FROM Item i " +
            "WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "   OR LOWER(i.itemComment) LIKE LOWER(CONCAT('%', :query, '%'))")

    
    Page<Item> searchByKeyword(@Param("query") String query, Pageable pageable);
}
