package zerobase.MyShoppingMall.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import zerobase.MyShoppingMall.type.BoardCategory;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class UserBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String password;

    @Enumerated(EnumType.STRING)
    private BoardCategory category;

    @Lob
    private String content;

    private int viewCount = 0;
    private boolean isSecret = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

}
