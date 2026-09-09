package com.unicom.visionhub.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;


public class JwtUtil {


    private static final String DEFAULT_SECRET = "ZJ_Unicom";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000;


    private static final SecretKey KEY = Keys.hmacShaKeyFor(
            DigestUtils.sha256(resolveSecret().getBytes(StandardCharsets.UTF_8)));


    private static String resolveSecret() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = System.getProperty("JWT_SECRET");
        }
        return (secret == null || secret.isBlank()) ? DEFAULT_SECRET : secret;
    }


    public static String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .claims(claims)
                .signWith(KEY)
                .compact();
    }


    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }


    public static String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }
}