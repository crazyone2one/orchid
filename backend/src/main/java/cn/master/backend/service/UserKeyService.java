package cn.master.backend.service;

import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.RefreshTokenResponse;
import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserKey;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

/**
 * 用户api key 服务层。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
public interface UserKeyService extends IService<UserKey> {
    UserKey createRefreshToken(String username, String accessToken);

    UserKey verifyExpiration(UserKey userKey);

    Optional<UserKey> findByToken(String token);

    String getRefreshTokenFromCookies(HttpServletRequest request);

    RefreshTokenResponse generateNewToken(RefreshTokenRequest refreshTokenRequest);

    void deleteRefreshToken(String refreshToken);
}
