//package zerobase.weather.domain;
//
//import jakarta.persistence.*;
//import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.List;
//
//@Table(name = "users")
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class User implements UserDetails {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", updatable = false)
//    private Long id;
//
//    @Column(name = "email", nullable = false, unique = true)
//    private String email;
//
//    @Column(name = "password", nullable = false)
//    private String password;
//
//    @Builder
//    public User(String email, String password, String auth) {
//        this.email = email;
//        this.password = password;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority("user"));
//    }
//
//    @Override
//    public String getUsername() {
//        return email;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    //계정 만료 여부
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    //계정 잠금 여부
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    //패스워드 만료 여부
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    //계정 사용 여부
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
