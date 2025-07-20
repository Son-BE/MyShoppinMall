//package zerobase.MyShoppingMall.service.member;
//
//import lombok.Getter;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import zerobase.MyShoppingMall.entity.Member;
//import zerobase.MyShoppingMall.type.Role;
//
//import java.util.Collection;
//import java.util.List;
//
//@Getter
//public class CustomUserDetails implements UserDetails {
//    private final Member member;
//
//    public CustomUserDetails(Member member)  {
//        this.member = member;
//    }
//
//    public Long getPoint() {
//        System.out.println("로그인 사용자의 포인트: " + member.getPoint());
//        return member.getPoint();
//    }
//
//    @Override
//    public String getUsername() {
//        return member.getEmail();
//    }
//
//    public String getNickname() {
//        return member.getNickName();
//    }
//
//    public Role hasRole(String role) {
//        return member.getRole();
//    }
//
//    @Override
//    public String getPassword() {
//        return member.getPassword();
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return UserDetails.super.isAccountNonExpired();
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));
//    }
//
//
//}
