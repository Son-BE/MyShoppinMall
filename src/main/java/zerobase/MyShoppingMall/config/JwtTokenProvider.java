//package zerobase.MyShoppingMall.config;
//
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//
//@Component
//public class JwtTokenProvider {
//
//    private final Key secretKey;
//    private final long validityInMilliseconds = 1800000;
//
//    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
//        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//    public String createToken(String username) {
//        Date now = new Date();
//        Date validity = new Date(now.getTime() + validityInMilliseconds);
//
//        return Jwts.builder()
//                .setSubject(username)
//                .setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(secretKey, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//
//    public String resolveToken(HttpServletRequest request) {
//        String bearer = request.getHeader("Authorization");
//        if (bearer != null && bearer.startsWith("Bearer ")) {
//            return bearer.substring(7);
//        }
//        return null;
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
//            return true;
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    public String getUsername(String token) {
//        return Jwts.parserBuilder().setSigningKey(secretKey)
//                .build().parseClaimsJws(token).getBody().getSubject();
//    }
//}
