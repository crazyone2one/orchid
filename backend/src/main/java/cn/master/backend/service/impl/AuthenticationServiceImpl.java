package cn.master.backend.service.impl;

import cn.master.backend.entity.UserKey;
import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.payload.response.AuthenticationResponse;
import cn.master.backend.security.JwtGenerator;
import cn.master.backend.security.UserDetailsServiceImpl;
import cn.master.backend.service.AuthenticationService;
import cn.master.backend.service.UserKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtGenerator jwtGenerator;
    private final UserKeyService userKeyService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        String accessToken = jwtGenerator.generateToken(request.getUsername(), userDetailsService.loadUserByUsername(request.getUsername()).getAuthorities());
        UserKey userKey = userKeyService.createRefreshToken(request.getUsername(), accessToken);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(userKey.getRefreshToken())
                .build();
    }
}
