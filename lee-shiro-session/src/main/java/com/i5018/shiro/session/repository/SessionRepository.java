package com.i5018.shiro.session.repository;

import org.apache.shiro.session.Session;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author limk
 * @date 2020/9/2 8:33
 */
public interface SessionRepository {

    /**
     * 保存会话
     *
     * @param session {@link org.apache.shiro.session.Session}
     */
    void saveSession(Session session);

    /**
     * 更新会话
     *
     * @param session {@link org.apache.shiro.session.Session}
     */
    void updateSession(Session session);

    /**
     * 刷新缓存重新计算过期时间
     *
     * @param sessionId 会话ID
     */
    void refreshSession(Serializable sessionId);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     */
    void deleteSession(Serializable sessionId);

    /**
     * 获取会话
     *
     * @param sessionId 会话ID
     * @return {@link org.apache.shiro.session.Session}
     */
    Session getSession(Serializable sessionId);

    /**
     * 获取所有会话
     *
     * @return {@link org.apache.shiro.session.Session}
     */
    Collection<Session> getAllSessions();

    /**
     * 获取所有会话数
     *
     * @return {@link org.apache.shiro.session.Session} count
     */
    Long getActiveSessionsSize();

}
