package zerobase.MyShoppingMall.repository.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Override
    Optional<Item> findById(Long itemId);

    Page<Item> findByCategory(ItemCategory category, Pageable pageable);
    Page<Item> findByGenderAndSubCategory(Gender gender, ItemSubCategory subCategory, Pageable pageable);
    Page<Item> findBySubCategory(ItemSubCategory subCategory, Pageable pageable);
    @EntityGraph(attributePaths = "itemImages")
    List<Item> findAll();
    Page<Item> findByGender(Gender gender, Pageable pageable);


}
