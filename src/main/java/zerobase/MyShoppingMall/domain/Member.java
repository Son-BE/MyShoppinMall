package zerobase.MyShoppingMall.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "nick_name", nullable = false)
    private String nickName;

    @Column(nullable = false)
    private String gender;

    private String role;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "delete_type", columnDefinition = "CHAR(1) DEFAULT 'N'")
    private String deleteType;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Address> addresses;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Order> orders;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Cart> cartItems;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<WishList> wishLists;

}
