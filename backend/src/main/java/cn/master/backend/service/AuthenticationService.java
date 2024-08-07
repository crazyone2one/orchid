package cn.master.backend.service;

import cn.master.backend.payload.request.AuthenticationRequest;
import cn.master.backend.payload.response.AuthenticationResponse;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
