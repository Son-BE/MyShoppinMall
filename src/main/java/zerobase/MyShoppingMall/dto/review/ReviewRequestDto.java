package zerobase.MyShoppingMall.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDto {
    private Long orderId;          // 주문 ID
    private Long orderDetailId;    // 주문 상세 ID
    private Long memberId;         // 작성자 ID
    private String content;        // 리뷰 내용
    private int rating;            // 평점
}
