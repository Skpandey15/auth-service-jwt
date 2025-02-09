package com.example.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_KEY = "5iDD6GOAqkn0+RaaAoO6zEe6iZathhWw6YxIP06Db2k="; // Base64-encoded key
    private static final long EXPIRATION_TIME = 3600000; // 1 hour

    // ✅ Generate Key
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ✅ Generate Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())  // ✅ FIX: Use `Jwts.SIG.HS256`
                .compact();
    }

    // ✅ Extract Username
    public String extractUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())  // ✅ FIX: `verifyWith` replaces `setSigningKey`
                .build()
                .parseSignedClaims(token)  // ✅ FIX: `parseSignedClaims()` replaces `parseClaimsJws()`
                .getPayload();
        return claims.getSubject();
    }

    // ✅ Validate Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())  // ✅ FIX: Use `verifyWith()`
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
