package zerobase.MyShoppingMall.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import zerobase.MyShoppingMall.type.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int price;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    private ItemSubCategory subCategory;

    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    private Styles style;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Enumerated(EnumType.STRING)
    private Color primaryColor;

    @Enumerated(EnumType.STRING)
    private Color secondaryColor;

    private String imageUrl;
    private String itemComment;
    private int quantity;
    private int itemRating;
    @Builder.Default
    @Column
    private Integer reviewCount = 0;
    @Builder.Default
    @Column
    private Integer viewCount = 0;
    @Builder.Default
    @Column
    private Integer orderCount = 0;
    @Builder.Default
    @Column
    private Integer cartCount = 0;
    @Builder.Default
    @Column
    private Integer wishCount = 0;

    private String review;
    private char deleteType;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<StyleTag> styleTags = new HashSet<>();

    @Transient
    private boolean isWish;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;





    @OneToMany(mappedBy = "item")
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<WishList> wishLists;

    public boolean isWish() {
        return isWish;
    }

    public void setWish(boolean isWish) {
        this.isWish = isWish;
    }

    public ItemSubCategory getSubCategory() {
        return subCategory;
    }

    public String getImagePath() {
        return imageUrl;
    }
}
