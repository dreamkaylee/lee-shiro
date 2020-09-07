package com.i5018.shiro.authc.realm;

import com.i5018.shiro.authc.UserPrincipal;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author limk
 * @date 2020/8/31 22:38
 */
public abstract class BaseAuthorizingRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(BaseAuthorizingRealm.class);

    public BaseAuthorizingRealm() {
    }

    public BaseAuthorizingRealm(CacheManager cacheManager) {
        super(cacheManager);
    }

    public BaseAuthorizingRealm(CredentialsMatcher matcher) {
        super(matcher);
    }

    public BaseAuthorizingRealm(CacheManager cacheManager, CredentialsMatcher matcher) {
        super(cacheManager, matcher);
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (logger.isDebugEnabled()) {
            logger.debug("------------------------------------- authorize -------------------------------------");
        }

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        UserPrincipal user = (UserPrincipal) principals.getPrimaryPrincipal();
        info.addRoles(user.getRoles());
        info.addStringPermissions(user.getPermissions());
        return info;
    }

    @Override
    protected Object getAuthenticationCacheKey(AuthenticationToken token) {
        if (token != null) {
            Object principal = token.getPrincipal();
            return getCacheKey(principal);
        }

        return null;
    }

    @Override
    protected Object getAuthorizationCacheKey(PrincipalCollection principals) {
        return getCacheKey(super.getAuthorizationCacheKey(principals));
    }

    @Override
    protected Object getAuthenticationCacheKey(PrincipalCollection principals) {
        return getCacheKey(super.getAuthenticationCacheKey(principals));
    }

    private Object getCacheKey(Object principal) {
        if (principal instanceof String) {
            return principal;
        } else if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUsername();
        } else {
            return null;
        }
    }

}
