package cn.master.backend.security;

import cn.master.backend.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
public class AuthUserDetail implements UserDetails {
    @Getter
    private final String id;
    private final String username;
    private final String password;
    @Getter
    private final String organizationId;
    @Getter
    private final String projectId;
    Collection<? extends GrantedAuthority> authorities;

    public AuthUserDetail(User user, List<String> roles) {
        this.username = user.getName();
        this.password = user.getPassword();
        this.authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        this.id = user.getId();
        this.organizationId = user.getLastOrganizationId();
        this.projectId = user.getLastProjectId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
