package cn.master.backend.security;

import cn.master.backend.service.BaseUserRolePermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

/**
 * @author Created by 11's papa on 08/06/2024
 **/
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthorizationFilter jwtAuthorizationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final BaseUserRolePermissionService baseUserRolePermissionService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));
        //http.anonymous(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/auth/login", "/auth/refreshToken").permitAll();
            authorize.requestMatchers("/swagger-ui*/**", "/v3/api-docs/**").permitAll();
            authorize.anyRequest().authenticated();
        });
        http.authenticationProvider(authenticationProvider());
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(restAuthenticationEntryPoint));
        http.logout(tmp -> {
            //tmp.clearAuthentication(true); // 是否在退出时清除当前用户的认证信息
            //tmp.invalidateHttpSession(true); // 是否移除 HttpSession
            tmp.logoutUrl("/auth/logout").permitAll();
            tmp.addLogoutHandler(new CustomLogoutHandler())
                    .logoutSuccessHandler(new CustomLogoutSuccessHandler());
        });
        http.addFilterBefore(jwtAuthorizationFilter, LogoutFilter.class);
        http.addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(true));
        return http.build();

    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setHideUserNotFoundExceptions(false);
        return authenticationProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean("auth")
    public CustomPermissionEvaluator customPermissionEvaluator() {
        return new CustomPermissionEvaluator(baseUserRolePermissionService);
    }
}
