package cn.master.backend.service;

import cn.master.backend.payload.dto.user.UserDTO;
import cn.master.backend.payload.request.AuthenticationRequest;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
public interface AuthenticationService {
    UserDTO authenticate(AuthenticationRequest request);
}
