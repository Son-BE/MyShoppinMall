package zerobase.MyShoppingMall.entity;

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
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(name = "item_path")
    private String itemPath;

    public String getImagePath() {
        return this.itemPath;
    }
}
