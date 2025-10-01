package zerobase.MyShoppingMall.controller.review;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import zerobase.MyShoppingMall.dto.review.ReviewRequestDto;
import zerobase.MyShoppingMall.dto.review.ReviewResponseDto;
import zerobase.MyShoppingMall.entity.Review;
import zerobase.MyShoppingMall.service.review.ReviewService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewApiController {

    private final ReviewService reviewService;

    //1. 리뷰작성
    @PostMapping
    public ReviewResponseDto writeReview(@RequestBody ReviewRequestDto request) {
        Review review = reviewService.writeReview(
                request.getOrderId(),
                request.getOrderDetailId(),
                request.getMemberId(),
                request.getContent(),
                request.getRating()
        );

        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .itemId(review.getItem().getId())
                .content(review.getContent())
                .rating(review.getRating())
                .build();

    }

    //2. 리뷰 수정
    @PutMapping("/{reviewId}")
    public ReviewResponseDto updateReview(@PathVariable Long reviewId,
                                          @RequestBody ReviewRequestDto request) {

        Review review = reviewService.updateReview(
                reviewId,
                request.getMemberId(),
                request.getContent(),
                request.getRating()
        );

        return ReviewResponseDto.builder()
                .reviewId(review.getId())
                .itemId(review.getItem().getId())
                .content(review.getContent())
                .rating(review.getRating())
                .build();
    }

    //3. 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public void deleteReview(@PathVariable Long reviewId,
                             @RequestParam Long memberId) {
        reviewService.deleteReview(reviewId, memberId);
    }
}
