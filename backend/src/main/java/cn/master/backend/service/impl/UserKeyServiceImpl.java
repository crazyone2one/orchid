package cn.master.backend.service.impl;

import cn.master.backend.entity.UserKey;
import cn.master.backend.mapper.UserKeyMapper;
import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.RefreshTokenResponse;
import cn.master.backend.security.JwtGenerator;
import cn.master.backend.service.UserKeyService;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.Instant.now;

/**
 * 用户api key 服务层实现。
 *
 * @author 11's papa
 * @since 1.0.0 2024-08-06
 */
@Service
@RequiredArgsConstructor
public class UserKeyServiceImpl extends ServiceImpl<UserKeyMapper, UserKey> implements UserKeyService {
    private final JwtGenerator jwtGenerator;

    @Override
    public UserKey createRefreshToken(String username, String accessToken) {
        UserKey userKey = UserKey.builder().createUser(username)
                .enable(true)
                .refreshToken(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                .accessToken(accessToken)
                .expireTime(Date.from(now().plus(2, ChronoUnit.HOURS)).getTime())
                .forever(false)
                .build();
        mapper.insert(userKey);
        return userKey;
    }

    @Override
    public UserKey verifyExpiration(UserKey userKey) {
        if (userKey.getExpireTime() < System.currentTimeMillis()) {
            mapper.delete(userKey);
            throw new RuntimeException("refreshToken is expired");
        }
        return userKey;
    }

    @Override
    public Optional<UserKey> findByToken(String token) {
        return queryChain().where(UserKey::getRefreshToken).eq(token).oneOpt();
    }

    @Override
    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, "refreshToken");
        return Objects.nonNull(cookie) ? cookie.getValue() : "";
    }

    @Override
    public RefreshTokenResponse generateNewToken(RefreshTokenRequest request) {
        queryChain().where(UserKey::getRefreshToken).eq(request.getRefreshToken()).oneOpt()
                .map(this::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("refreshToken is invalid"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = jwtGenerator.generateToken(authentication);
        return RefreshTokenResponse.builder()
                .accessToken(token)
                .refreshToken(request.getRefreshToken())
                .build();
    }

    @Override
    public void deleteRefreshToken(String refreshToken) {
        findByToken(refreshToken).ifPresent(mapper::delete);
    }
}
