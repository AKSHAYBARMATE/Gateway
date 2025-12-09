// package com.gateway.config;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.JwtException;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import java.security.Key;
// import java.util.Date;

// @Service
// public class GatewayJwtService {

//     @Value("${jwt.secret}")
//     private String secret;

//     private Key getSignInKey() {
//         return Keys.hmacShaKeyFor(secret.getBytes());
//     }

//     public String extractUsername(String token) {
//         return parseClaims(token).getSubject();
//     }

//     public boolean isTokenValid(String token) {
//         try {
//             Claims claims = parseClaims(token);
//             Date exp = claims.getExpiration();
//             return exp != null && exp.after(new Date());
//         } catch (JwtException | IllegalArgumentException e) {
//             return false;
//         }
//     }

//     private Claims parseClaims(String token) {
//         return Jwts.parserBuilder()
//                 .setSigningKey(getSignInKey())
//                 .build()
//                 .parseClaimsJws(token)
//                 .getBody();
//     }
// }
