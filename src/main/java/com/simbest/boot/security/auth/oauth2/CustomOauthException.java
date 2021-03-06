/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.oauth2;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * 用途：自定义OAUTH2受保护的资源请求异常
 * 作者: lishuyi
 * 时间: 2018/8/29  21:49
 */
@Slf4j
@JsonSerialize(using = CustomOauthExceptionSerializer.class)
public class CustomOauthException extends OAuth2Exception {

    //1、小程序code获取openid失败返回码
    //2、小程序通过openid获取用户信息失败
    //3、小程序使用SessionKey解密用户手机号失败返回码
    public static final String OAUTH2_LOGIN_ERROR = "520";

    //小程序解析手机号成功后，手机号数据库用户不存在返回码
    public static final String OAUTH2_MINI_ERROR = "530";

    //没有access_token访问API时，返回403
    //access_token过期时，返回403
    public static final String OAUTH2_FORBIDDEN = "403";

    private String httpErrorCode;

    public CustomOauthException(String msg) {
        super(msg);
        this.httpErrorCode = OAUTH2_FORBIDDEN;
    }

    public CustomOauthException(String msg, String httpErrorCode) {
        super(msg);
        this.httpErrorCode = httpErrorCode;
    }

    /**
     * 与OauthExceptionEntryPoint配合
     * @return int
     */
    @Override
    public int getHttpErrorCode() {
        return Integer.valueOf(httpErrorCode);
    }

    @Override
    public String getSummary() {
        String result = super.getSummary();
        log.warn("OAuth2 认证过程出了点问题，即将组装返回的错误信息，状态码【{}】，错误信息【{}】", httpErrorCode, result);
        return result;
    }

}
