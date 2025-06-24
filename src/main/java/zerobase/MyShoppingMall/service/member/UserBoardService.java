package zerobase.MyShoppingMall.service.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.entity.UserBoard;
import zerobase.MyShoppingMall.repository.member.UserBoardRepository;
import zerobase.MyShoppingMall.type.BoardCategory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBoardService {

    private final UserBoardRepository userBoardRepository;

    public List<UserBoard> findAllBoards() {
        return userBoardRepository.findAllByOrderByCreatedAtDesc();
    }

    public UserBoard createBoard(UserBoard board, Member member) {
        board.setCreatedAt(LocalDateTime.now());
        board.setMember(member);
        return userBoardRepository.save(board);
    }

    @Transactional
    public UserBoard findBoardById(Long id) {
        UserBoard board = userBoardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("No board found with id: " + id));

        board.setViewCount((board.getViewCount() + 1));
        return board;

    }

    public List<UserBoard> findByMemberName(String memberName) {
        return userBoardRepository.findByMemberNickName(memberName);
    }

    public List<UserBoard> findBoardsByCategory(BoardCategory category) {
        return userBoardRepository.findByCategory(category);
    }

    public void deleteBoard(Long id, Member member) {
        UserBoard board = findBoardById(id);
        if(!board.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }
        userBoardRepository.delete(board);
    }

    public UserBoard updateBoard(Long id, String title, String content, boolean secret,Member member) {
        UserBoard board = findBoardById(id);
        if(!board.getMember().getId().equals(member.getId())) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        board.setTitle(title);
        board.setContent(content);
        board.setSecret(secret);
        board.setUpdatedAt(LocalDateTime.now());

        return userBoardRepository.save(board);
    }


}
