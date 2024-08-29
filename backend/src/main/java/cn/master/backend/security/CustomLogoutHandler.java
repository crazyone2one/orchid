package cn.master.backend.security;

import cn.master.backend.entity.UserKey;
import com.mybatisflex.core.query.QueryChain;
import com.mybatisflex.core.update.UpdateChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * @author Created by 11's papa on 08/28/2024
 **/
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        UserKey userKey = QueryChain.of(UserKey.class).where(UserKey::getAccessToken).eq(jwt).oneOpt().orElse(null);
        if (userKey != null) {
            UpdateChain.of(UserKey.class)
                    .set(UserKey::getRevoked, true)
                    .set(UserKey::getExpired, true)
                    .where(UserKey::getAccessToken).eq(jwt).update();
            SecurityContextHolder.clearContext();
        }
    }
}
