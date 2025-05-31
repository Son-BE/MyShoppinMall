package zerobase.MyShoppingMall.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
