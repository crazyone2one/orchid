package cn.master.backend.security;

import cn.master.backend.entity.UserKey;
import com.mybatisflex.core.query.QueryChain;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtGenerator jwtGenerator;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/refreshToken",
            "/auth/logout"
    );
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Check if the request is for a public endpoint
        if (isPublicEndpoint(request)) {
            // If the request is for a public endpoint, skip the filter
            log.info("Skipping the filter for the following request URL {}", request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            // Handle the case where the Authorization header is missing or does not contain a valid JWT
            handleMissingToken(response, request);
            return;
        }
        String token = authHeader.substring(7);
        String username = extractUsernameFromJwt(token);
        if (Objects.isNull(username)) {
            handleInvalidToken(response);
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
        Boolean isTokenValid = QueryChain.of(UserKey.class).where(UserKey::getAccessToken).eq(token).oneOpt()
                .map(t -> !t.getExpired() && !t.getRevoked()).orElse(false);
        if (!isTokenValid && jwtGenerator.validateToken(token, userDetails)) {
            handleInvalidToken(response);
            return;
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }

    private String extractUsernameFromJwt(String token) {
        try {
            return jwtGenerator.getUsernameFromToken(token);
        } catch (ExpiredJwtException exception) {
            log.warn("JWT has expired: {}", exception.getMessage());
            return null;
        }
    }

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        log.warn("JWT is not valid");
        response.setStatus(SC_UNAUTHORIZED);
        response.getWriter().write("JWT is not valid");
    }

    private void handleMissingToken(HttpServletResponse response, HttpServletRequest request) throws IOException {
        log.error("Authorization header is missing or does not contain a valid JWT for the following URL: {}", request.getServletPath());
        response.sendError(SC_UNAUTHORIZED, "Authorization header is missing or does not contain a valid JWT");
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(request.getServletPath()::startsWith);
    }
}
