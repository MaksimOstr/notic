package com.notic.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.*;
import java.util.function.Function;


@Service
public class JwtService {
    @Value("${JWT_SECRET:123412121212121212}")
    private String jwtSecret;

    @Value("${ISSUER:some@gmail.com}")
    private String issuer;

    @Value("${JWT_EXPIRE_IN:600000}")
    private long jwtExpirationTime;

    @PostConstruct
    private void validateSecret() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
    }

    public String getJwsToken(Collection<String> authorities, long subjectId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authorities);
        return generateJwtToken(claims, subjectId);
    }

    private String generateJwtToken(Map<String, Object> claims, long subjectId) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(Long.toString(subjectId))
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = getAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder().requireIssuer(issuer).setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
