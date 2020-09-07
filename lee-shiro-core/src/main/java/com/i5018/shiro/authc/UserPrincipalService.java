package com.i5018.shiro.authc;

/**
 * @author limk
 * @date 2020/8/25 16:58
 */
public interface UserPrincipalService {

    /**
     * 根据用户账号查询 UserPrincipal
     *
     * @param username 用户账号
     * @return {@link UserPrincipal }
     */
    UserPrincipal loadUserPrincipalByUsername(String username);

}
