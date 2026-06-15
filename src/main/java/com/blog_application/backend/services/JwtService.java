package com.blog_application.backend.services;

import java.util.Date;
import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
@Slf4j
public class JwtService {

    @Value("${secret_key}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 30;    /* 30 hrs */

    private SecretKey getSignInKey() {
        log.info("JWT SERVICE GET SIGNIN KEY METHOD CALLED");
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        log.info("KEYBYTES ARRAY = " + keyBytes);
        SecretKey secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("SECRET KEY GENERATED = " + secretKey);
        return secretKey;
    }

    public String generateToken(String email) {
        log.info("GENERATING AUTHENTICATION TOKEN");
        String token = Jwts.builder()
                .subject(email)
                .claim("username", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey())
                .compact();

        log.info("JWT SERVICE TOKEN GENERATED = " + token);
        return token;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch(JwtException e) {
            return false;
        }
    }
}
