package com.synshami.sonique.service;

import com.synshami.sonique.config.SpotifyProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthStateService {

    private final SpotifyProperties spotifyProperties;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(
                spotifyProperties.getStateSecret().getBytes()
        );
    }

    public String generateState(Long userId) {

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("nonce", UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + spotifyProperties.getStateExpiration())
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long validateAndExtractUserId(String state) {

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(state)
                    .getBody();

            return Long.parseLong(claims.getSubject());

        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid or expired OAuth state");
        }
    }
}