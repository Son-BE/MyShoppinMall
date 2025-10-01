package zerobase.MyShoppingMall.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.Order;
import zerobase.MyShoppingMall.entity.OrderDetail;
import zerobase.MyShoppingMall.entity.Review;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.item.ReviewRepository;
import zerobase.MyShoppingMall.repository.order.OrderRepository;
import zerobase.MyShoppingMall.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public Review writeReview(Long orderId, Long orderDetailId, Long memberId, String content, int itemRating) {
        Order order = orderRepository.findById(orderId).orElseThrow(()-> new RuntimeException("주문을 찾을 수 없습니다."));

        //1. 본인 확인
        if(!order.getMember().getId().equals(memberId)) {
            throw new RuntimeException("본인의 주문이 아닙니다.");
        }

        //2. 배송확인
        if (!(order.getOrderStatus() == OrderStatus.DELIVERED
                || order.getOrderStatus() == OrderStatus.ORDERED)) {
            throw new RuntimeException("리뷰는 배송 완료 또는 구매 확정 상태에서만 가능합니다.");
        }

        //3. 작성가능 기간
        LocalDateTime deadline = order.getDeliveredAt().plusDays(7);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuntimeException("리뷰 작성 기간(7일)이 지났습니다");
        }

        // 4. 주문 상세 가져오기
        OrderDetail orderDetail = order.getOrderDetails().stream()
                .filter(od -> od.getId().equals(orderDetailId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 주문 상세를 찾을 수 없습니다."));

        // 5. 중복 작성 방지
        if (reviewRepository.existsByOrderDetail(orderDetail)) {
            throw new RuntimeException("이미 리뷰를 작성한 상품입니다.");
        }

        Item item = orderDetail.getItem();

        //6. 리뷰 저장
        Review review = new Review();
        review.setMember(order.getMember());
        review.setOrder(order);
        review.setOrderDetail(orderDetail);
        review.setItem(item);
        review.setContent(content);
        review.setRating(itemRating);
        review.setCreatedAt(LocalDateTime.now());


        Review savedReview = reviewRepository.save(review);

        item.updateRatingOnAdd(itemRating);
        itemRepository.save(item);

        return savedReview;
    }

    @Transactional
    public Review updateReview(Long reviewId, Long memberId, String content, int rating) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("본인 리뷰만 수정 가능");
        }

        review.setContent(content);
        review.setRating(rating);
        review.setUpdatedAt(LocalDateTime.now());

        // 아이템 평점 업데이트
        updateItemStats(review.getItem());

        return review;
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 없음"));

        if (!review.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 리뷰만 삭제 가능");
        }

        Item item = review.getItem();
        OrderDetail orderDetail = review.getOrderDetail();

        // 연관관계 해제
        orderDetail.setReview(null);

        reviewRepository.delete(review);

        // 아이템 평점 업데이트
        updateItemStats(item);
    }


    private void updateItemStats(Item item) {
        List<Review> reviews = reviewRepository.findByItem(item);

        int reviewCount = reviews.size();
        double avgRating = reviewCount == 0 ? 0.0 :
                reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        item.setReviewCount(reviewCount);
        item.setItemRating((int) avgRating);

        itemRepository.save(item);
    }


}
