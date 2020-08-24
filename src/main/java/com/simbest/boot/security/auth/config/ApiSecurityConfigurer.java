/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.config;

import com.simbest.boot.base.service.IOauth2ClientDetailsService;
import com.simbest.boot.security.IAuthService;
import com.simbest.boot.security.auth.oauth2.Oauth2RedisTokenStore;
import com.simbest.boot.security.auth.oauth2.CustomWebResponseExceptionTranslator;
import com.simbest.boot.security.auth.oauth2.OauthExceptionEntryPoint;
import com.simbest.boot.security.auth.oauth2.PhoneCodeTokenGranter;
import com.simbest.boot.security.auth.oauth2.PhoneTokenGranter;
import com.simbest.boot.security.auth.oauth2.RsaPasswordTokenGranter;
import com.simbest.boot.security.auth.oauth2.UumsTokenGranter;
import com.simbest.boot.security.auth.oauth2.WxmaBindTokenGranter;
import com.simbest.boot.security.auth.oauth2.WxmaCodeTokenGranter;
import com.simbest.boot.security.auth.oauth2.WxmaMiniTokenGranter;
import com.simbest.boot.security.auth.provider.GenericAuthenticationChecker;
import com.simbest.boot.security.auth.oauth2.Oauth2TokenServices;
import com.simbest.boot.util.encrypt.RsaEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用途：RESTFul 接口安全配置
 * 参考：
 * http://blog.didispace.com/spring-security-oauth2-xjf-1/
 * http://niocoder.com/2018/04/29/Spring-Boot-2.0-%E6%95%B4%E5%90%88-Spring-Security-Oauth2/
 * https://github.com/longfeizheng/springboot2.0-oauth2
 * 作者: lishuyi
 * 时间: 2018/1/20  11:24
 * <p>
 * 获取token请求（/oauth/token），请求所需参数：client_id、client_secret、grant_type
 * client模式：         http://localhost:8080/uums/oauth/token?grant_type=client_credentials&scope=all&client_id=password_changer&client_secret=338d6311c8efad8314b9212860adb12e
 * password模式：       http://localhost:8080/uums/oauth/token?grant_type=password&scope=all&client_id=password_changer&client_secret=338d6311c8efad8314b9212860adb12e&username=hadmin&password=111.com
 * rsa加密password模式： http://localhost:8080/uums/oauth/token?grant_type=rsapassword&scope=all&client_id=password_changer&client_secret=338d6311c8efad8314b9212860adb12e&username=hadmin&password=nvXgA1H+5GLFVP9Fi4vFEZLRU8WzNC2XOAC2lOieB6T9B1rNPzWp1PNs5WptaX3ipQ8znbtn8RdIjDFkVhLx09hvAgWEjQFzYStMk8qAhQWUXa7bZOtC6RQDFJ0WEuDLPyF9CSjN5o3DrEVnNqzTRAHfeP2AYK+N0MsVCI5DkcQ=
 * uums认证password模式：http://localhost:6001/andhall/oauth/token?grant_type=uumspassword&scope=all&client_id=simbest_andhall&client_secret=338d6311c8efad8314b9212860adb12e&appcode=andhall&username=zhengxiaolei&password=pVOMsZTPznKFDU82BNN4BtaALbM4eQ/FphXo77BdSXnn+TTJU2yPqbVKhFoHCG5Kgcm7OuBBlhykzaRgUwGAn7xTXEKdU/uE3bBr4jRDKeOXJ8LxMyelqXFnPVOhF5/GqGCCbafp40BwMUDKdxqdqRVttZmjEST27DmX1U9Hqdc=
 * 注意：
 *  1、client的password的有两种方式：1、BCryptPasswordEncoder加密前的MD5值  2、SecurityUtils万能密码的MD5值
 *  2、client_secret对Simbest_2018做MD5加密得：338d6311c8efad8314b9212860adb12e，而数据库中client_secret需要进行BCryptPasswordEncoder加密后存放，即 $2a$12$.s06wfoiyPGWRCH6xwfdlOt8h.eWQXz97ZQQ/RSFjeReArJy8Ymg2
 *  3、uumspassword方式的password的值因为到主数据认证，所以需要对password进行RSA加密
 *
 * http://andaily.com/blog/?p=528 返回格式
 *
 * 刷新token请求（/oauth/token），请求所需参数：grant_type、refresh_token、client_id、client_secret
 * 注意：client模式没有refresh_token
 * http://localhost:8080/uums/oauth/token?grant_type=refresh_token&client_id=password_changer&client_secret=e10adc3949ba59abbe56e057f20f883e&appcode=nma&refresh_token=fbde81ee-f419-42b1-1234-9191f1f95be9
 *
 * 检查token请求
 * http://localhost:8080/uums/oauth/check_token?token=d1034046-064d-4b5f-b9a6-c0f66abba28d
 *
 * client service的grant_type调整时，redis清理前缀为OAUTH2_CLIENT_PREFIX:,因此执行下述命令：
 * 127.0.0.1:6379> keys OAUTH2_CLIENT_PREFIX:*
 * 127.0.0.1:6379> del OAUTH2_CLIENT_PREFIX:simbest_andhall
 */
@Configuration
@Order(20)
public class ApiSecurityConfigurer {

    @Autowired
    @Qualifier(BeanIds.AUTHENTICATION_MANAGER)
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomWebResponseExceptionTranslator customWebResponseExceptionTranslator;

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        final OAuth2AccessDeniedHandler handler = new OAuth2AccessDeniedHandler();
        handler.setExceptionTranslator(customWebResponseExceptionTranslator);
        return handler;
    }

    @Configuration
    @EnableResourceServer
    protected class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId("*").authenticationEntryPoint(new OauthExceptionEntryPoint());
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers("/**/api/**")
                    .and()
                    //解决ERR no such key when RedisOperationsSessionRepository.saveChangeSessionId
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)

                    .and()
                    .authorizeRequests()
                    .antMatchers("/**/api/**")
                    .authenticated()
                    .and().httpBasic()
                    .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler()).authenticationEntryPoint(new OauthExceptionEntryPoint());
        }

    }



    @Configuration
    @EnableAuthorizationServer
    protected class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private IAuthService authService;

        @Autowired
        private Oauth2RedisTokenStore redisTokenStore;

        @Autowired
        private IOauth2ClientDetailsService oauth2ClientDetailsService;

        @Autowired
        private CustomWebResponseExceptionTranslator exceptionTranslator;

        @Autowired
        private RsaEncryptor encryptor;

        @Autowired
        private GenericAuthenticationChecker genericAuthenticationChecker;

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.withClientDetails(oauth2ClientDetailsService);
    }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
            security
                    .tokenKeyAccess("permitAll()")
//                    .checkTokenAccess("isAuthenticated()")
                    .checkTokenAccess("permitAll()")
                    .allowFormAuthenticationForClients()
                    .authenticationEntryPoint(new OauthExceptionEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler());
        }

        private void addUserDetailsService(DefaultTokenServices tokenServices, UserDetailsService userDetailsService) {
            if (userDetailsService != null) {
                PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
                provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<PreAuthenticatedAuthenticationToken>(
                        userDetailsService));
                tokenServices
                        .setAuthenticationManager(new ProviderManager(Arrays.<AuthenticationProvider> asList(provider)));
            }
        }

        @Bean
        public Oauth2TokenServices createOauth2TokenServices() {
            Oauth2TokenServices tokenServices = new Oauth2TokenServices();
            tokenServices.setTokenStore(redisTokenStore);
            tokenServices.setSupportRefreshToken(true);
            tokenServices.setReuseRefreshToken(true);
            tokenServices.setClientDetailsService(oauth2ClientDetailsService);
//            tokenServices.setTokenEnhancer(tokenEnhancer());
            addUserDetailsService(tokenServices, authService);
            tokenServices.setGenericAuthenticationChecker(genericAuthenticationChecker);
            return tokenServices;
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            // 配置tokenStore，保存到redis缓存中
            endpoints.authenticationManager(authenticationManager)
                    .tokenStore(redisTokenStore)
                    .tokenServices(createOauth2TokenServices())
                    .tokenGranter(tokenGranter(endpoints))
                    // 不添加userDetailsService，刷新access_token时会报错
                    .userDetailsService(authService)
                    .exceptionTranslator(exceptionTranslator);
        }

        /**
         * 追加自定义OAUTH2方式的认证
         * 参考：https://github.com/spring-projects/spring-security-oauth/issues/564
         * @param endpoints
         * @return
         */
        private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
            List<TokenGranter> granters = new ArrayList<>(Arrays.asList(endpoints.getTokenGranter()));
            //追加rsapassword方式的认证
            granters.add(new RsaPasswordTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), encryptor, genericAuthenticationChecker));
            //追加uumspassword方式的认证
            granters.add(new UumsTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
            //追加wxmabind方式的认证
            granters.add(new WxmaBindTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
            //追加wxmacode方式的认证
            granters.add(new WxmaCodeTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
            //追加wxmamini方式的认证
            granters.add(new WxmaMiniTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory()));
            //追加phone方式的认证
            granters.add(new PhoneTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), encryptor));
            //追加phonecode方式的认证
            granters.add(new PhoneCodeTokenGranter(authenticationManager, endpoints.getTokenServices(),
                    endpoints.getClientDetailsService(), endpoints.getOAuth2RequestFactory(), encryptor));
            return new CompositeTokenGranter(granters);
        }

    }


}
