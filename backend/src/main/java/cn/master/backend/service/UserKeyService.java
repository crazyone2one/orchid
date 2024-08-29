package cn.master.backend.service;

import com.mybatisflex.core.service.IService;
import cn.master.backend.entity.UserKey;
import org.springframework.security.core.userdetails.UserDetails;

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

    void revokeAllUserTokens(UserDetails userDetails);

    void saveUserToken(String accessToken, String refreshToken, UserDetails userDetails);
}
