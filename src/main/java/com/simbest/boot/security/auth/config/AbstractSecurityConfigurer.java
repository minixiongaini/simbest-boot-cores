/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import com.simbest.boot.security.auth.provider.SsoUsernameAuthenticationProvider;
import com.simbest.boot.security.auth.service.SysUserInfoFullService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 用途：
 * 作者: lishuyi
 * 时间: 2018/1/20  11:24
 */
public abstract class AbstractSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private SysUserInfoFullService sysUserInfoService;

    @Autowired
    private SsoUsernameAuthenticationProvider ssoUsernameAuthenticationProvider;

    @Value("${security.auth.manager.username}")
    private String managerUsername;

    @Value("${security.auth.manager.password}")
    private String managerPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 默认密码加密长度10
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     *
     * @param auth 认证管理器构造器AuthenticationManagerBuilder
     * @throws Exception 异常
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser(managerUsername)
                // 注意：https://spring.io/blog/2017/11/01/spring-security-5-0-0-rc1-released#password-storage-updated
                .password(managerPassword)
                .roles("ACT_MGMT", "USER");
        //仅基于用户名验证
        auth.authenticationProvider(ssoUsernameAuthenticationProvider);
        //基于用户名和密码验证
        auth.userDetailsService(sysUserInfoService).passwordEncoder(passwordEncoder());
    }

}