package cn.master.backend.service.impl;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserKey;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.mapper.UserKeyMapper;
import cn.master.backend.payload.request.RefreshTokenRequest;
import cn.master.backend.payload.response.RefreshTokenResponse;
import cn.master.backend.security.JwtGenerator;
import cn.master.backend.service.UserKeyService;
import com.mybatisflex.core.query.QueryChain;
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
        User user = QueryChain.of(User.class).where(User::getName).eq(username).one();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class)
                .where(UserRoleRelation::getUserId).eq(user.getId())
                .list();
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).toList();
        UserKey userKey = UserKey.builder().userId(username)
                .enable(true)
                .refreshToken(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                //.accessToken(accessToken)
                .expireTime(Date.from(now().plus(1, ChronoUnit.DAYS)).getTime())
                .forever(false)
                .roles(roleList)
                .build();
        mapper.insert(userKey);
        return userKey;
    }

    @Override
    public UserKey verifyExpiration(UserKey userKey) {
        if (userKey.getExpireTime() < System.currentTimeMillis()) {
            mapper.delete(userKey);
            throw new RuntimeException(userKey.getRefreshToken() + " Refresh token is expired. Please make a new login..!");
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
