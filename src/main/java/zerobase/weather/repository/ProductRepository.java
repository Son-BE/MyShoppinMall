package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.weather.domain.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContaining(String keyword);
}
