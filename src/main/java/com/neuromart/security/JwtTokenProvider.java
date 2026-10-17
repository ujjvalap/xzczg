package com.neuromart.security;

import com.neuromart.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expireMillis;
    private final String issuer;
    private final String audience;

    public JwtTokenProvider(
            @Value("${neuromart.jwt.secret:ujjvalpateliya@170105}") String secret,
            @Value("${neuromart.jwt.expire-hours:48}") long expireHours,
            @Value("${neuromart.jwt.issuer:NeuroMarth_Backend}") String issuer,
            @Value("${neuromart.jwt.audience:http://localhost:5173}") String audience
    ) {
        this.expireMillis = expireHours * 3600 * 1000;
        this.issuer = issuer;
        this.audience = audience;

        // Ensure key is at least 256 bits by hashing secret with SHA-256
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
            this.key = Keys.hmacShaKeyFor(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expireMillis);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", String.valueOf(user.getId()))
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String idStr = claims.get("id", String.class);
        if (idStr != null) {
            return Long.parseLong(idStr);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
