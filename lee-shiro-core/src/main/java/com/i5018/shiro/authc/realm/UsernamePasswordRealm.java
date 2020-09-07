package com.i5018.shiro.authc.realm;

import com.i5018.shiro.authc.UserPrincipal;
import com.i5018.shiro.authc.UserPrincipalService;
import com.i5018.shiro.constant.ShiroUserStateEnum;
import com.i5018.shiro.exception.ExpiredAccountException;
import com.i5018.shiro.util.BaseByteSource;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户名密码认证
 *
 * @author limk
 * @date 2020/8/25 15:38
 */
public class UsernamePasswordRealm extends BaseAuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordRealm.class);

    private final UserPrincipalService userPrincipalService;

    public UsernamePasswordRealm(CacheManager cacheManager, CredentialsMatcher matcher, UserPrincipalService userPrincipalService) {
        super(cacheManager, matcher);
        this.userPrincipalService = userPrincipalService;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (logger.isDebugEnabled()) {
            logger.debug("------------------------------------- authenticate -------------------------------------");
        }

        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) token;

        // 用户账号
        String username = usernamePasswordToken.getUsername();

        UserPrincipal user = userPrincipalService.loadUserPrincipalByUsername(username);
        if (user == null) {
            throw new UnknownAccountException("账号 [" + username + "] 不存在");
        }

        // 账号被锁定
        if (user.getState().equals(ShiroUserStateEnum.LOCKED.getValue())) {
            throw new LockedAccountException("账号 [" + username + "] 被锁定.");
        }

        // 账号被禁用
        if (user.getState().equals(ShiroUserStateEnum.DISABLED.getValue())) {
            throw new LockedAccountException("账号 [" + username + "] 被禁用.");
        }

        // 账号已过期
        if (user.getState().equals(ShiroUserStateEnum.EXPIRED.getValue())) {
            throw new ExpiredAccountException("账号 [" + username + "] 已过期.");
        }

        user.setPassword(null);
        user.setSalt(null);

        return new SimpleAuthenticationInfo(user,
                user.getPassword(), new BaseByteSource(user.getSalt()), this.getName());
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

}
