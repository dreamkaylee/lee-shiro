package com.i5018.shiro.authc;

/**
 * @author limk
 * @date 2020/8/25 16:58
 */
public interface UserPrincipalSmsService {

    /**
     * 根据用户手机号查询 UserPrincipal
     *
     * @param sms 用户手机号
     * @return {@link UserPrincipal }
     */
    UserPrincipal loadUserPrincipalBySms(String sms);

}
