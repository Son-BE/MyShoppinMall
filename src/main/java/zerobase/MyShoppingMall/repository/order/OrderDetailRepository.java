package zerobase.MyShoppingMall.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.MyShoppingMall.domain.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
