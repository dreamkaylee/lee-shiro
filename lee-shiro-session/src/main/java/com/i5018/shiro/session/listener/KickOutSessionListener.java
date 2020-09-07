package com.i5018.shiro.session.listener;

import com.i5018.shiro.constant.ShiroConstant;
import com.i5018.shiro.cache.ExpiredCache;
import com.i5018.shiro.cache.ExpiredCacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Deque;

/**
 * 用户并发登录监听器
 *
 * @author limk
 * @date 2020/8/25 10:14
 */
public class KickOutSessionListener implements SessionListener {

    private static final Logger logger = LoggerFactory.getLogger(KickOutSessionListener.class);

    private final ExpiredCache<String, Deque<Serializable>> cache;

    public KickOutSessionListener(ExpiredCacheManager cacheManager) {
        this.cache = cacheManager.getCache(ShiroConstant.DEFAULT_KICK_OUT_CACHE);
    }

    @Override
    public void onStart(Session session) {

    }

    @Override
    public void onStop(Session session) {
        this.removeSessionFromCache(session);
    }

    @Override
    public void onExpiration(Session session) {
        this.removeSessionFromCache(session);
    }

    private void removeSessionFromCache(Session session) {
        String username = (String) session.getAttribute(ShiroConstant.DEFAULT_KICK_OUT_SUBJECT);
        if (username != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("remove session: " + session.getId() + " in deque of " + username
                        + "@kickOutSessionCache");
            }

            synchronized (cache) {
                Deque<Serializable> deque = cache.get(username);
                deque.remove(session.getId());
                cache.put(username, deque);
            }
        }
    }

}
