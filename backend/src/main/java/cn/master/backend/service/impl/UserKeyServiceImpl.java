package cn.master.backend.service.impl;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserKey;
import cn.master.backend.entity.UserRoleRelation;
import cn.master.backend.mapper.UserKeyMapper;
import cn.master.backend.service.UserKeyService;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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
@Transactional(rollbackOn = Exception.class)
public class UserKeyServiceImpl extends ServiceImpl<UserKeyMapper, UserKey> implements UserKeyService {

    @Override
    public UserKey createRefreshToken(String username, String accessToken) {
        User user = QueryChain.of(User.class).where(User::getName).eq(username).one();
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class)
                .where(UserRoleRelation::getUserId).eq(user.getId())
                .list();
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).toList();
        UserKey userKey = UserKey.builder().userId(username)
                .refreshToken(Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes()))
                .accessToken(accessToken)
                .expireTime(Date.from(now().plus(1, ChronoUnit.DAYS)).getTime())
                .expired(false).revoked(false)
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
        return queryChain().where(UserKey::getAccessToken).eq(token).oneOpt();
    }


    @Override
    public void revokeAllUserTokens(UserDetails userDetails) {
        List<UserKey> userKeys = QueryChain.of(UserKey.class).where(UserKey::getUserId).eq(userDetails.getUsername()).list();
        if (CollectionUtils.isNotEmpty(userKeys)) {
            userKeys.forEach(userKey -> {
                userKey.setRevoked(true);
                userKey.setExpired(true);
                mapper.update(userKey);
            });
        }
    }

    @Override
    public void saveUserToken(String accessToken, String refreshToken, UserDetails userDetails) {
        UserKey userKey = UserKey.builder().userId(userDetails.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expireTime(Date.from(now().plus(1, ChronoUnit.DAYS)).getTime())
                .expired(false).revoked(false)
                .roles(new ArrayList<>())
                .build();
        mapper.insert(userKey);
    }
}
