package cn.master.backend.controller;

import cn.master.backend.entity.UserKey;
import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.AuthenticationResponse;
import cn.master.backend.security.JwtGenerator;
import cn.master.backend.service.AuthenticationService;
import cn.master.backend.service.UserKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserKeyService userKeyService;
    private final JwtGenerator jwtGenerator;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/info")
    public ResponseEntity<String> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(authentication.getName());
    }

    @PostMapping("/refreshToken")
    public AuthenticationResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Optional<UserKey> token = userKeyService.findByToken(request.getRefreshToken());
        Collection<? extends GrantedAuthority> authorities;
        if (token.isPresent()) {
            authorities = token.get().getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        } else {
            authorities = List.of();
        }
        return token
                .map(userKeyService::verifyExpiration)
                .map(UserKey::getUserId)
                .map(user -> {
                    String accessToken = jwtGenerator.generateToken(user, authorities);
                    return AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getRefreshToken())
                            .build();
                }).orElseThrow(() -> new RuntimeException("Refresh Token is not in DB..!!"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        if (Objects.nonNull(request.getRefreshToken())) {
            userKeyService.deleteRefreshToken(request.getRefreshToken());
        }
        return ResponseEntity.ok().build();
    }
}
