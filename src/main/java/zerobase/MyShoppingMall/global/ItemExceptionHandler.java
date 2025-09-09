package zerobase.MyShoppingMall.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ItemExceptionHandler {

    @ExceptionHandler(ItemNotFoundException.class)
    public String handleItemNotFound(ItemNotFoundException e, Model model) {
        log.warn("상품을 찾을 수 없습니다:{}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/404";
    }

    @ExceptionHandler(InvalidItemException.class)
    public String handleInvalidItem(InvalidItemException e, Model model) {
        log.warn("잘못된 상품 요청입니다:{}", e.getMessage());
        model.addAttribute("errorMessage", e.getMessage());
        return "error/400";
    }

    @ExceptionHandler(ItemServiceException.class)
    public String handleItemService(ItemServiceException e, Model model) {
        log.error("상품 서비스 오류입니다",e);
        model.addAttribute("errorMessage", "서비스 처리 중 오류가 발생했습니다.");
        return "error/500";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception e, Model model) {
        log.error("예상치 못한 오류가 발생했습니다.", e);
        model.addAttribute("errorMessage", "시스템 오류가 발생했습니다.");
        return "error/500";
    }


}
