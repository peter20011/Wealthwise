package com.example.wealthwise_api.Util;

import com.example.wealthwise_api.Entity.Token;
import com.example.wealthwise_api.Repository.JWTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class JWTUtil {

    Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    JWTokenRepository jwtTokenRepository;

    private static final String SECRET_KEY=
            "maka_987654321_maka_987654321_maka_987654321_maka_987654321";

    public JWTUtil(JWTokenRepository jwtTokenRepository) {
        this.jwtTokenRepository = jwtTokenRepository;
    }

    public String issueToken(String subject) {
        return issueToken(subject, Map.of());
    }

    public String issueToken(String subject, String ...scopes) {
        return issueToken(subject, Map.of("scopes", scopes));
    }

    public String issueToken(String subject, List<String> scopes) {
        return issueToken(subject, Map.of("scopes", scopes));
    }

    public String issueToken(String subject,Map<String, Object> claims){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(
                        Date.from(Instant.now().plus(1,DAYS))
                ).signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public boolean isTokenValid(String jwt, String username) {

        String subject = getSubject(jwt);
        return subject.equals(username) && !isTokenExpired(jwt) && !isTokenOnBlackList(jwt);
    }

    private boolean isTokenOnBlackList(String jwt) {
        return jwtTokenRepository.existsByToken(jwt);

    }

    private boolean isTokenExpired(String jwt) {
        Date today = Date.from(Instant.now());
        return getClaims(jwt).getExpiration().before(today);
    }

    public void setTokenToList(String jwt) {
        Token token = new Token(jwt ,getClaims(jwt).getExpiration());
    }

}