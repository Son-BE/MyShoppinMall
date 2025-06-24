package zerobase.MyShoppingMall.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.MyShoppingMall.entity.UserBoard;
import zerobase.MyShoppingMall.type.BoardCategory;

import java.util.List;

@Repository
public interface UserBoardRepository extends JpaRepository<UserBoard, Long> {
    List<UserBoard> findAllByOrderByCreatedAtDesc();

    List<UserBoard> findByMemberNickName(String memberName);

    List<UserBoard> findByCategory(BoardCategory category);
}
