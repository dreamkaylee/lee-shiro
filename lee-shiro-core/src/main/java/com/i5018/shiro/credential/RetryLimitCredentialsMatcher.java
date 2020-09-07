package com.i5018.shiro.credential;

import com.i5018.shiro.cache.ExpiredCache;
import com.i5018.shiro.cache.ExpiredCacheManager;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author limk
 * @date 2020/8/23 22:17
 */
public class RetryLimitCredentialsMatcher extends HashedCredentialsMatcher {

    /**
     * 密码错误重试缓存
     */
    public static final String DEFAULT_PASSWORD_RETRY_CACHE = "passwordRetryCache";

    /**
     * 密码错误重试缓存
     */
    private final ExpiredCache<String, AtomicInteger> passwordRetryCache;

    /**
     * 密码最大错误次数
     */
    private int maxErrorsCount = 5;

    /**
     * 缓存时长
     */
    private Duration expiration = Duration.ofSeconds(7200);

    public RetryLimitCredentialsMatcher(ExpiredCacheManager cacheManager) {
        passwordRetryCache = cacheManager.getCache(DEFAULT_PASSWORD_RETRY_CACHE);
    }

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        String username = (String) token.getPrincipal();
        // 获取用户登录次数
        AtomicInteger retryCount = passwordRetryCache.get(username);

        if (retryCount == null) {
            // 如果用户没有登陆过，登陆次数加1并放入缓存
            retryCount = new AtomicInteger(0);
        } else {
            int value = retryCount.incrementAndGet();
            if (value >= maxErrorsCount) {
                // 如果用户登陆失败次数大于{maxErrorsCount}次 抛出锁定用户异常
                throw new ExcessiveAttemptsException("您的密码连续" + maxErrorsCount + "次输入错误，账号已被锁定！");
            }
            retryCount = new AtomicInteger(value);
        }
        passwordRetryCache.put(username, retryCount, expiration);

        // 判断用户账号和密码是否正确
        boolean matches = super.doCredentialsMatch(token, info);
        if (matches) {
            // 如果正确,从缓存中将用户登录计数 清除
            passwordRetryCache.remove(username);
        }
        return matches;
    }

    public void setMaxErrorsCount(int maxErrorsCount) {
        this.maxErrorsCount = maxErrorsCount;
    }

    public void setExpiration(Duration expiration) {
        this.expiration = expiration;
    }

}
