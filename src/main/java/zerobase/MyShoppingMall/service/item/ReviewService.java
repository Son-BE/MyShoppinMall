package zerobase.MyShoppingMall.service.item;

import zerobase.MyShoppingMall.dto.item.ReviewRequestDto;

public interface ReviewService {
    // 작성 가능 여부 확인
    boolean canWriteReview(Long memberId, Long itemId, Long orderId);

    // 리뷰 저장
    void saveReview(ReviewRequestDto dto, Long memberId);

}
