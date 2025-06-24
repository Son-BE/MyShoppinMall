package zerobase.MyShoppingMall.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import zerobase.MyShoppingMall.type.Gender;
import zerobase.MyShoppingMall.type.ItemCategory;
import zerobase.MyShoppingMall.type.ItemSubCategory;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_comment", nullable = false)
    private String itemComment;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "delete_type", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private char deleteType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String review;

    private int itemRating;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToMany(mappedBy = "item")
    @JsonIgnore
    private List<OrderDetail> orderDetails;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<WishList> wishLists;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<ItemImage> itemImages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSubCategory subCategory;


}
