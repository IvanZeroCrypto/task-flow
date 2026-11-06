package com.example.task_flow.security;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service 
public class JwtService{

	@Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessExpiration;

    public String generateAccessToken(Long userId,String email){
    	return Jwts.builder()
    			.subject(String.valueOf(userId))
    			.claim("email", email)
    			.issuedAt(new Date())
                .id(UUID.randomUUID().toString())
    			.expiration(new Date(System.currentTimeMillis() + accessExpiration))
    			.signWith(Keys.hmacShaKeyFor(secret.getBytes()), Jwts.SIG.HS256)
    			.compact();
    }

    public Long extractUserId(String token){
    	return Long.valueOf(extractAllClaims(token).getSubject());
    }

    public String extractEmail(String token){
    	return extractAllClaims(token).get("email",String.class);
    }

    public boolean isTokenValid(String token){
    	try {
    		extractAllClaims(token);
    		return true;
    	} catch (JwtException | IllegalArgumentException e) {
    		return false;
    	}
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getExpirationAccess(String token){
        Claims claims = extractAllClaims(token);
        Date expiration = claims.getExpiration();
        Long ttl = expiration.getTime() - System.currentTimeMillis();

        return Math.max(ttl,0);
    }
}