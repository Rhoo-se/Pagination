package com.project.bulletin_board.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component // Spring 컨테이너에 Bean으로 등록
public class JwtTokenProvider {

    private final SecretKey key; // io.jsonwebtoken.Key 대신 SecretKey 사용 (JJWT 0.12.x)
    private final long expirationMs;

    // 1. application.properties에서 값을 읽어와서 생성자에서 주입
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        // Base64로 인코딩된 비밀 키를 디코딩하여 SecretKey 객체로 변환
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * 2. Subject(email)을 받아 Access Token을 생성
     */
    public String createToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(subject) // 토큰의 주체 (보통 email 또는 user id)
                .issuedAt(now) // 토큰 발급 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .signWith(key) // 1번에서 만든 Key로 서명
                .compact();
    }

    /**
     * 3. Access Token을 파싱(해독)하여 Subject(email)를 추출
     */
    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key) // 1번에서 만든 Key로 검증
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 4. Access Token의 유효성 검증 (만료 여부, 변조 여부 등)
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // (실제 운영 시에는 e.printStackTrace() 대신 Log.warn() 등을 사용하세요)
            // e.printStackTrace();
            return false;
        }
    }
}