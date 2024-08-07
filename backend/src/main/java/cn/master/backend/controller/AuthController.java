package cn.master.backend.controller;

import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.AuthenticationResponse;
import cn.master.backend.payload.response.RefreshTokenResponse;
import cn.master.backend.service.AuthenticationService;
import cn.master.backend.service.UserKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserKeyService userKeyService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshTokenCookie(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse refreshTokenResponse = userKeyService.generateNewToken(request);
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(refreshTokenResponse.getAccessToken())
                .refreshToken(request.getRefreshToken())
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        if (Objects.nonNull(request.getRefreshToken())) {
            userKeyService.deleteRefreshToken(request.getRefreshToken());
        }
        return ResponseEntity.ok().build();
    }
}
