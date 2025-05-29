package zerobase.MyShoppingMall.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.domain.Item;
import zerobase.MyShoppingMall.type.Category;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    @Override
    Optional<Item> findById(Long itemId);

    Page<Item> findByCategory(Category category, Pageable pageable);
}
