package zerobase.MyShoppingMall.entity;

import jakarta.persistence.*;
import lombok.*;
import zerobase.MyShoppingMall.type.BehaviorType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;
    private Long itemId;

    @Enumerated(EnumType.STRING)
    private BehaviorType behaviorType;

    private LocalDateTime timestamp;

    public MemberBehavior(Long memberId, Long itemId, BehaviorType behaviorType, LocalDateTime now) {
        this.memberId = memberId;
        this.itemId = itemId;
        this.behaviorType = behaviorType;
        this.timestamp = now;
    }
}
