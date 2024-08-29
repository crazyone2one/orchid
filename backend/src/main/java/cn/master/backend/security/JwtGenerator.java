package cn.master.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.time.Instant.now;
import static java.util.stream.Collectors.joining;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Slf4j
@Component
public class JwtGenerator {
    private static final String AUTHORITIES_KEY = "auth";
    public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";

    private SecretKey key() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String username, Collection<? extends GrantedAuthority> authorities) {
        return createToken(username, authorities, 6, ChronoUnit.HOURS);
    }

    public String generateRefreshToken(String username) {
        return createToken(username, List.of(), 5, ChronoUnit.DAYS);
    }

    private String createToken(String username, Collection<? extends GrantedAuthority> authorities, long expiration, ChronoUnit unit) {
        var claimsBuilder = Jwts.claims().subject(username);
        if (!authorities.isEmpty()) {
            claimsBuilder.add(AUTHORITIES_KEY, authorities.stream().map(GrantedAuthority::getAuthority).collect(joining(",")));
        }
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(username)
                .claims(claimsBuilder.build())
                .issuer("orchid")
                .issuedAt(Date.from(now()))
                .expiration(Date.from(now().plus(expiration, unit)))
                .signWith(key()).compact();
    }

    public String generateAccessToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        var claimsBuilder = Jwts.claims().subject(username);
        if (!authorities.isEmpty()) {
            claimsBuilder.add(AUTHORITIES_KEY, authorities.stream().map(GrantedAuthority::getAuthority).collect(joining(",")));
        }
        return createToken(username, authorities, 1, ChronoUnit.MINUTES);
    }

    public Jws<Claims> claims(String token) {
        return Jwts.parser().verifyWith(key())
                .build()
                .parseSignedClaims(token);
    }

    public String getUsernameFromToken(String token) {
        return claims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Jws<Claims> claims = claims(token);
            Date tokenExpiration = claims.getPayload().getExpiration();
            if (tokenExpiration.before(new Date())) {
                return false;
            }
            return claims.getPayload().getSubject().equals(userDetails.getUsername());
        } catch (JwtException exception) {
            log.error("Invalid JWT token: {}", exception.getMessage());
        }
        return false;
    }

}
