package cn.master.backend.security;

import cn.master.backend.entity.User;
import cn.master.backend.entity.UserRoleRelation;
import com.mybatisflex.core.query.QueryChain;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = QueryChain.of(User.class).where(User::getName).eq(username)
                .oneOpt()
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
        List<UserRoleRelation> userRoleRelations = QueryChain.of(UserRoleRelation.class)
                .where(UserRoleRelation::getUserId).eq(user.getId())
                .list();
        List<String> roleList = userRoleRelations.stream().map(UserRoleRelation::getRoleId).toList();
        return new CustomUserDetails(user, roleList);
    }
}
