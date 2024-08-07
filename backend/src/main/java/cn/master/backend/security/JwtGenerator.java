package cn.master.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import static java.time.Instant.now;
import static java.util.stream.Collectors.joining;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Slf4j
@Component
public class JwtGenerator {
    private static final String AUTHORITIES_KEY = "roles";
    public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";
    private final long expiration = 1L;

    private SecretKey key() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(username)
                .issuer("orchid")
                .issuedAt(Date.from(now()))
                .expiration(Date.from(now().plus(expiration, ChronoUnit.HOURS)))
                .signWith(key()).compact();
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        var claimsBuilder = Jwts.claims().subject(username);
        if (!authorities.isEmpty()) {
            claimsBuilder.add(AUTHORITIES_KEY, authorities.stream().map(GrantedAuthority::getAuthority).collect(joining(",")));
        }
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(authentication.getName())
                .claims(claimsBuilder.build())
                .issuer("orchid")
                .issuedAt(Date.from(now()))
                .expiration(Date.from(now().plus(expiration, ChronoUnit.HOURS)))
                .signWith(key()).compact();
    }

    public Jws<Claims> claims(String token) {
        return Jwts.parser().verifyWith(key())
                .build()
                .parseSignedClaims(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Jws<Claims> claims = claims(token);
            Date tokenExpiration = claims.getPayload().getExpiration();
            return tokenExpiration.before(new Date()) && claims.getPayload().getSubject().equals(userDetails.getUsername());
        } catch (JwtException exception) {
            log.error("Invalid JWT token: {}", exception.getMessage());
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();

        Object authoritiesClaim = claims.get(AUTHORITIES_KEY);

        Collection<? extends GrantedAuthority> authorities = authoritiesClaim == null ? AuthorityUtils.NO_AUTHORITIES
                : AuthorityUtils.commaSeparatedStringToAuthorityList(authoritiesClaim.toString());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
