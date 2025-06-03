package zerobase.MyShoppingMall.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_image")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ItemImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "item_path", nullable = false)
    private String itemPath;

    public String getImagePath() {
        return this.itemPath;
    }
}
