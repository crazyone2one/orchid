package cn.master.backend.controller;

import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.AuthenticationResponse;
import cn.master.backend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @GetMapping("/info")
    //@PreAuthorize("@auth.hasAuthority('ORGANIZATION_MEMBER:READ,ORGANIZATION_MEMBER:READ') and @auth.hasAuthority('x')")
    //@HasAuthorize(value = PermissionConstants.SYSTEM_USER_ROLE_READ)
    //@HasAnyAuthority(value = {PermissionConstants.SYSTEM_USER_ROLE_READ, PermissionConstants.SYSTEM_USER_READ}, logical = Logical.OR)
    public ResponseEntity<String> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(authentication.getName());
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(authenticationService.refreshToken(request, response));
    }

    //@PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok().build();
    }
}
