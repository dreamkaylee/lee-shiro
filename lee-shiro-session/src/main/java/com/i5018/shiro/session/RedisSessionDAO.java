package com.i5018.shiro.session;

import com.i5018.shiro.session.repository.SessionRepository;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * @author limk
 * @date 2020/9/1 16:44
 */
public class RedisSessionDAO extends CachingSessionDAO {

    private static final Logger logger = LoggerFactory.getLogger(RedisSessionDAO.class);

    private final SessionRepository sessionRepository;

    public RedisSessionDAO(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    /**
     * <p>
     * 重写CachingSessionDAO中readSession方法，如果Session中没有登陆信息就调用doReadSession方法从Redis中重读
     * session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null 代表没有登录，登录后Shiro会放入该值
     * </p>
     *
     * @param sessionId 会话ID
     * @return {@link org.apache.shiro.session.Session}
     * @throws UnknownSessionException 找不到Session异常
     */
    @Override
    public Session readSession(Serializable sessionId) throws UnknownSessionException {
        Session session = super.getCachedSession(sessionId);

        // 如果缓存不存在或者缓存中没有登陆认证后记录的信息就重新从Redis中读取
        if (session == null || session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY) == null) {
            session = this.doReadSession(sessionId);
            if (session == null) {
                throw new UnknownSessionException("There is no session with id [" + sessionId + "]");
            } else {
              // 缓存
              cache(session, session.getId());
            }
        }
        return session;
    }

    /**
     * 从Redis中读取Session,并重置过期时间
     *
     * @param sessionId 会话ID
     * @return {@link org.apache.shiro.session.Session}
     */
    @Override
    protected Session doReadSession(Serializable sessionId) {
        Session session = null;
        try {
            session = sessionRepository.getSession(sessionId);
            if (session != null) {
                // 重置 Redis中缓存过期时间
                sessionRepository.refreshSession(sessionId);

                if (logger.isDebugEnabled()) {
                    logger.debug("read and refreshed sessionId {}", sessionId);
                }
            }
        } catch (Exception e) {
            logger.error("read and refreshed Session error", e);
        }

        return session;
    }

    /**
     * 从Redis中读取，但不重置Redis中缓存过期时间
     *
     * @param sessionId 会话ID
     * @return {@link org.apache.shiro.session.Session}
     */
    public Session doReadSessionWithoutExpire(Serializable sessionId) {
        Session session = null;

        try {
            session = sessionRepository.getSession(sessionId);

            if (logger.isDebugEnabled()) {
                logger.debug("read sessionId {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("read Session error", e);
        }

        return session;
    }

    /**
     * DefaultSessionManager在创建完session后会调用该方法
     *
     * @param session {@link org.apache.shiro.session.Session}
     * @return 会话ID
     */
    @Override
    protected Serializable doCreate(Session session) {
        // 创建一个Id并设置给Session
        Serializable sessionId = this.generateSessionId(session);

        super.assignSessionId(session, sessionId);

        try {
            sessionRepository.saveSession(session);

            if (logger.isDebugEnabled()) {
                logger.debug("create sessionId {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("create session error", e);
        }

        return sessionId;
    }

    /**
     * 更新会话
     * <p>更新会话最后访问时间/停止会话/设置超时时间/设置移除属性等会调用</p>
     *
     * @param session {@link org.apache.shiro.session.Session}
     */
    @Override
    protected void doUpdate(Session session) {
        //如果会话过期/停止 没必要再更新了
        try {
            if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
                return;
            }

            if (session instanceof ShiroSession) {
                // 如果没有主要字段(除lastAccessTime以外其他字段)发生改变
                ShiroSession ss = (ShiroSession) session;
                if (!ss.isChanged()) {
                    return;
                }

                ss.setChanged(false);
                ss.setLastAccessTime(new Date());

                sessionRepository.updateSession(session);

                if (logger.isDebugEnabled()) {
                    logger.debug("update sessionId {}", session.getId());
                }
            }
        } catch (Exception e) {
            logger.error("update session error", e);
        }
    }

    /**
     * 删除会话
     * <p>会话过期/会话停止（如用户退出时）会调用</p>
     *
     * @param session {@link org.apache.shiro.session.Session}
     */
    @Override
    protected void doDelete(Session session) {
        try {
            sessionRepository.deleteSession(session.getId());

            // 删除缓存
            super.uncache(session);

            if (logger.isDebugEnabled()) {
                logger.info("remove sessionId {}", session.getId());
            }
        } catch (Exception e) {
            logger.error("remove session error", e);
        }
    }

    /**
     * 获取当前所有活跃用户，如果用户量多此方法影响性能
     */
    @Override
    public Collection<Session> getActiveSessions() {
        Collection<Session> sessions = null;
        try {
            sessions = sessionRepository.getAllSessions();
        } catch (Exception e) {
            logger.error("get active sessions error.", e);
        }
        return sessions;
    }

    /**
     * 获取存货session的数量
     *
     * @return count
     */
    public Long getActiveSessionsSize() {
        Long size = 0L;
        try {
            size = sessionRepository.getActiveSessionsSize();
        } catch (Exception e) {
            logger.error("get active sessions error.", e);
        }
        return size;
    }

}
