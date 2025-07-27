package zerobase.MyShoppingMall.controller.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import zerobase.MyShoppingMall.entity.Comment;
import zerobase.MyShoppingMall.entity.UserBoard;
import zerobase.MyShoppingMall.oAuth2.CustomUserDetails;
import zerobase.MyShoppingMall.repository.member.UserBoardRepository;
import zerobase.MyShoppingMall.service.member.CommentService;
import zerobase.MyShoppingMall.service.member.UserBoardService;
import zerobase.MyShoppingMall.type.BoardCategory;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class UserBoardController {

    private final UserBoardService userBoardService;
    private final UserBoardRepository userBoardRepository;
    private final CommentService commentService;

    @GetMapping
    public String list(@RequestParam(required = false) BoardCategory category, Model model) {
        List<UserBoard> boards;
        String categoryName = "게시판";

        if(category != null) {
            boards = userBoardRepository.findByCategory(category);
            model.addAttribute("selectedCategory", category);
            switch (category) {
                case NOTICE:
                    categoryName = "공지사항";
                    break;
                case SHIPPING:
                    categoryName = "배송 문의";
                    break;
                case QNA:
                    categoryName = "자주 묻는 질문";
                    break;
                case RETURN:
                    categoryName = "교환/환불 문의";
                    break;
                case EVENT:
                    categoryName = "이벤트";
                    break;
                case FREE:
                    categoryName = "자유게시판";
                    break;

                default:
                    categoryName = category.name();
            }
        } else {
            boards = userBoardService.findAllBoards();
        }

        model.addAttribute("boards", boards);
        model.addAttribute("categories", BoardCategory.values());
        model.addAttribute("categoryName", categoryName);
        return "board/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserBoard userBoard = userBoardService.findBoardById(id);

        if(userBoard.isSecret()) {
            String username = userDetails != null ? userDetails.getUsername() : null;
            boolean isOwner = username != null && username.equals(userBoard.getMember().getNickName());
            boolean isAdmin = userDetails != null && userDetails.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

            if(isOwner && isAdmin) {
                return "error/403";
            }
        }

        List<Comment> comments = commentService.getComments(id);
        model.addAttribute("board", userBoard);
        model.addAttribute("comments", comments);
        return "board/detail";
    }

    @GetMapping("/write")
    public String writeForm(@RequestParam(required = false) BoardCategory category,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            RedirectAttributes redirectAttributes,
                            Model model) {

        if (category != null) {
            if (isAdminOnlyCategory(category) && !isAdmin(userDetails)) {
                redirectAttributes.addFlashAttribute("error", "해당 게시판은 관리자만 글을 작성할 수 있습니다.");
                return "redirect:/board?category=" + category;
            }
        }

        model.addAttribute("board", new UserBoard());
        model.addAttribute("categories", BoardCategory.values());
        return "board/write";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        UserBoard board = userBoardService.findBoardById(id);
        model.addAttribute("board", board);
        return "board/edit";
    }

    @GetMapping("/my-posts")
    public String myPosts(Model model,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
            String username = userDetails.getNickname();
            List<UserBoard> myBoards = userBoardService.findByMemberName(username);
            model.addAttribute("myBoards", myBoards);
            return "board/my-posts";
    }

    @GetMapping("/secret/{id}")
    public String showPasswordForm(@PathVariable Long id, Model model) {
        model.addAttribute("boardId", id);
        return "board/password-form";
    }


    @PostMapping("/write")
    public String write(@ModelAttribute UserBoard userBoard,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        RedirectAttributes redirectAttributes) {

        if (isAdminOnlyCategory(userBoard.getCategory()) && !isAdmin(userDetails)) {
            redirectAttributes.addFlashAttribute("error", "해당 게시판은 관리자만 글을 작성할 수 있습니다.");
            return "redirect:/board?category=" + userBoard.getCategory();
        }

        userBoardService.createBoard(userBoard, userDetails.getMember());
        return "redirect:/board?category=" + userBoard.getCategory();
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam String content,
                       @RequestParam(defaultValue = "false") boolean secret,
                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        userBoardService.updateBoard(id, title, content, secret, userDetails.getMember());
        return "redirect:/board";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        userBoardService.deleteBoard(id, userDetails.getMember());
        return "redirect:/board";
    }

    @PostMapping("/secret/{id}")
    public String checkPassword(@PathVariable Long id,
                                @RequestParam String password,
                                RedirectAttributes redirectAttributes) {
        Optional<UserBoard> boardOpt = userBoardRepository.findById(id);

        if (boardOpt.isPresent()) {
            UserBoard board = boardOpt.get();

            if (board.getPassword() != null && board.getPassword().equals(password)) {
                return "redirect:/board/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "비밀번호가 틀렸습니다.");
                return "redirect:/board/secret/" + id;
            }
        }

        return "redirect:/board";
    }

    @PostMapping("/{id}/comment")
    public String writeComment(@PathVariable Long id,
                               @RequestParam String content,
                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserBoard board = userBoardService.findBoardById(id);
        commentService.writeComment(content, board, userDetails.getMember());
        return "redirect:/board/" + id;
    }

    // 관리자 권한 체크
    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) return false;
        return userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    // 관리자만 글쓰기
    private boolean isAdminOnlyCategory(BoardCategory category) {
        return category == BoardCategory.NOTICE
                || category == BoardCategory.QNA
                || category == BoardCategory.EVENT;
    }

}
