package zerobase.MyShoppingMall.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import zerobase.MyShoppingMall.domain.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);
}
