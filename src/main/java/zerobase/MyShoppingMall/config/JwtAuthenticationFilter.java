//package zerobase.MyShoppingMall.config;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetails;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import zerobase.MyShoppingMall.service.member.CustomUserDetailsService;
//
//import java.io.IOException;
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JwtTokenProvider jwtTokenProvider;
//    private final CustomUserDetailsService userDetailsService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String token = jwtTokenProvider.resolveToken(request);
//
//        if(token != null && jwtTokenProvider.validateToken(token)) {
//            String username = jwtTokenProvider.getUsername(token);
//
//            var userDetails = userDetailsService.loadUserByUsername(username);
//            var auth = new UsernamePasswordAuthenticationToken(
//                    userDetails, null, userDetails.getAuthorities());
//
//            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(auth);
//
//            filterChain.doFilter(request, response);
//        }
//    }
//}
