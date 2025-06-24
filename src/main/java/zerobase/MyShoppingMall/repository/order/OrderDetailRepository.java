package zerobase.MyShoppingMall.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.OrderDetail;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
