package cn.master.backend.service;

import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
public interface AuthenticationService {
    UserDTO authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
