package zerobase.MyShoppingMall.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.domain.Comment;
import zerobase.MyShoppingMall.domain.Member;
import zerobase.MyShoppingMall.domain.UserBoard;
import zerobase.MyShoppingMall.repository.member.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;


    public void writeComment(String content, UserBoard board, Member member) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setBoard(board);
        comment.setMember(member);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    public List<Comment> getComments(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);
    }
}
