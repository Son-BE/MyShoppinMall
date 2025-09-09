package zerobase.MyShoppingMall.service.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import zerobase.MyShoppingMall.dto.item.ReviewRequestDto;
import zerobase.MyShoppingMall.entity.Item;
import zerobase.MyShoppingMall.entity.Member;
import zerobase.MyShoppingMall.entity.Order;
import zerobase.MyShoppingMall.entity.Review;
import zerobase.MyShoppingMall.repository.item.ItemRepository;
import zerobase.MyShoppingMall.repository.item.ReviewRepository;
import zerobase.MyShoppingMall.repository.member.MemberRepository;
import zerobase.MyShoppingMall.repository.order.OrderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;

    @Override
    public boolean canWriteReview(Long memberId, Long itemId, Long orderId) {
        // 1) 주문 존재 확인
        Order order = orderRepository.findById(orderId)
                .orElse(null);
        if (order == null || !order.getMember().getId().equals(memberId)) {
            return false;
        }

        // 2) 주문에 해당 아이템 포함 확인
        boolean itemInOrder = order.getOrderDetails().stream()
                .anyMatch(orderItem -> orderItem.getItem().getId().equals(itemId));
        if (!itemInOrder) return false;

        // 3) 주문 후 7일 이내 확인
        LocalDate orderDate = order.getCreatedAt().toLocalDate();
        if (ChronoUnit.DAYS.between(orderDate, LocalDate.now()) > 7) {
            return false;
        }

        // 4) 이미 리뷰 작성했는지 확인
        boolean exists = reviewRepository.existsByMemberIdAndItemIdAndOrderId(memberId, itemId, orderId);
        if (exists) return false;

        return true;
    }

    @Override
    public void saveReview(ReviewRequestDto dto, Long memberId) {
        if (!canWriteReview(memberId, dto.getItemId(), dto.getOrderId())) {
            throw new IllegalStateException("리뷰 작성 권한이 없습니다.");
        }

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 없습니다."));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 없습니다."));

        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("해당 주문건이 없습니다."));


        Review review = Review.builder()
                .item(item)
                .member(member)
                .order(order)
                .rating(dto.getRating())
                .content(dto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }

    @Override
    public void updateReview(Long reviewId, ReviewRequestDto dto, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if (!review.getMember().equals(memberId)) {
            throw new SecurityException("권한이 없습니다.");
        }

        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setUpdatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다."));

        if (!review.getMember().equals(memberId)) {
            throw new SecurityException("권한이 없습니다.");
        }

        reviewRepository.delete(review);
    }

    public double getAverageRating(Long itemId) {
        List<Review> reviews = reviewRepository.findByItemId(itemId);
        if (reviews.isEmpty()) return 0.0;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
    }

}
