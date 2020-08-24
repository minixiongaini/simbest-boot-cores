/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.security.auth.service;

import com.simbest.boot.security.IPermission;
import com.simbest.boot.security.IUser;
import com.simbest.boot.util.redis.RedisUtil;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * 用途：提供系统认证用户的缓存操作服务
 * 作者: lishuyi
 * 时间: 2019/10/14  15:32
 */
public interface IAuthUserCacheService {

    //===================================处理用户信息 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户信息，并按照KeyType的定义，提供username、preferredMobile、openid三组键值定位
     * @param user
     */
    void saveOrUpdateCacheUser(IUser user);

    /**
     * 尝试从缓存中读取用户信息
     * @param keyword
     * @return IUser
     */
    IUser loadCacheUser(String keyword);


    /**
     * 清理用户缓存
     * @param user
     */
    void removeCacheUser(IUser user);
    //===================================处理用户信息 END=========================================================//


    //===================================处理用户应用权限 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户权限
     * @param username
     * @param appcode
     * @param permissions
     */
    void saveOrUpdateCacheUserPermission(String username, String appcode, Set<IPermission> permissions);

    /**
     * 尝试从缓存中读取用户权限
     * @param username
     * @param appcode
     * @return Set<IPermission>
     */
    Set<IPermission> loadCacheUserPermission(String username, String appcode);

    /**
     * 清理用户权限
     * @param username
     * @param appcode
     */
    void removeCacheUserPermission(String username, String appcode);
    //===================================处理用户应用权限 END=========================================================//



    //===================================处理用户应用访问 START=========================================================//
    /**
     * 在缓存中新增或覆盖更新用户应用访问
     * @param username
     * @param appcode
     * @param isPermit
     */
    void saveOrUpdateCacheUserAccess(String username, String appcode, Boolean isPermit);

    /**
     * 尝试从缓存中读取用户应用访问
     * @param username
     * @param appcode
     * @return Boolean
     */
    Boolean loadCacheUserAccess(String username, String appcode);

    /**
     * 清理用户应用访问
     * @param username
     * @param appcode
     */
    void removeCacheUserAccess(String username, String appcode);
    //===================================处理用户应用访问 END=========================================================//


    //===================================处理用户密码 START=========================================================//

    /**
     * 更新用户密码
     * @param username
     * @param password
     * @param isRight
     */
    void saveOrUpdateCacheUserPassword(String username, String password, Boolean isRight);

    /**
     * 尝试从缓存中读取用户密码
     * @param username
     * @param password
     * @return Boolean
     */
    Boolean loadCacheUserPassword(String username, String password);

    /**
     * 清理用户密码
     * @param username
     */
    void removeCacheUserPassword(String username);
    //===================================处理用户密码 END=========================================================//

    /**
     * 获取用户密码
     * @param username
     * @return
     */
    Set<String> getCacheUserPassword(String username);

    /**
     * 1、清理用户信息
     * 2、清理用户权限信息
     * 3、清理用户访问信息
     * @param username
     */
    void removeCacheUserAllInformaitions(String username);
}
