package com.i5018.shiro.authc.realm;

import com.i5018.shiro.authc.UserPrincipal;
import com.i5018.shiro.authc.UserPrincipalSmsService;
import com.i5018.shiro.constant.ShiroUserStateEnum;
import com.i5018.shiro.authc.token.SmsToken;
import com.i5018.shiro.exception.ExpiredAccountException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.cache.CacheManager;

/**
 * 短信验证码认证
 *
 * @author limk
 * @date 2020/8/25 17:06
 */
@Slf4j
public class SmsRealm extends BaseAuthorizingRealm {

    private final UserPrincipalSmsService userPrincipalSmsService;

    public SmsRealm(CacheManager cacheManager, UserPrincipalSmsService userPrincipalSmsService) {
        super(cacheManager);
        this.userPrincipalSmsService = userPrincipalSmsService;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        log.debug("------------------------------------- authenticate -------------------------------------");

        SmsToken smsToken = (SmsToken) token;

        // 用户手机号
        String sms = smsToken.getSms();

        UserPrincipal user = userPrincipalSmsService.loadUserPrincipalBySms(sms);
        if (user == null) {
            throw new UnknownAccountException("Sms [" + sms + "] not found");
        }

        // 账号被锁定
        if (user.getState().equals(ShiroUserStateEnum.LOCKED)) {
            throw new LockedAccountException("Sms [" + sms + "] is locked.");
        }

        // 账号被禁用
        if (user.getState().equals(ShiroUserStateEnum.DISABLED)) {
            throw new LockedAccountException("Sms [" + sms + "] is disabled.");
        }

        // 账号已过期
        if (user.getState().equals(ShiroUserStateEnum.EXPIRED)) {
            throw new ExpiredAccountException("Sms [" + sms + "] is expired.");
        }

        return new SimpleAuthenticationInfo(user, "N/A", this.getName());
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof SmsToken;
    }

}
