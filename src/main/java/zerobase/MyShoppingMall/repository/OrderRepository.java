package zerobase.MyShoppingMall.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.domain.Order;
import zerobase.MyShoppingMall.dto.Order.OrderRequestDto;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

}
