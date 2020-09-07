package com.i5018.shiro.session.listener;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author limk
 * @date 2020/9/2 8:32
 */
public class ShiroSessionListener implements SessionListener {

    private static final Logger logger = LoggerFactory.getLogger(ShiroSessionListener.class);

    private final SessionDAO sessionDAO;

    public ShiroSessionListener(SessionDAO sessionDAO) {
        this.sessionDAO = sessionDAO;
    }

    @Override
    public void onStart(Session session) {
        // 会话创建时触发
        if (logger.isDebugEnabled()) {
            logger.debug("create session {}", session.getId());
        }
    }

    @Override
    public void onStop(Session session) {
        sessionDAO.delete(session);
        // 会话被停止时触发
        if (logger.isDebugEnabled()) {
            logger.debug("destroy session {}", session.getId());
        }
    }

    @Override
    public void onExpiration(Session session) {
        sessionDAO.delete(session);
        //会话过期时触发
        if (logger.isDebugEnabled()) {
            logger.debug("session {} has expired", session.getId());
        }
    }

}
