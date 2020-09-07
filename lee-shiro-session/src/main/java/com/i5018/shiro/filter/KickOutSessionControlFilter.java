package com.i5018.shiro.filter;

import com.i5018.shiro.authc.UserPrincipal;
import com.i5018.shiro.constant.ShiroConstant;
import com.i5018.shiro.cache.ExpiredCache;
import com.i5018.shiro.cache.ExpiredCacheManager;
import com.i5018.shiro.util.WebUtil;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Deque;
import java.util.LinkedList;

/**
 * 用户并发登录过滤器
 *
 * @author limk
 * @date 2020/8/25 9:06
 */
public class KickOutSessionControlFilter extends AccessControlFilter {

    private static final Logger logger = LoggerFactory.getLogger(KickOutSessionControlFilter.class);

    /**
     * 踢出后到的地址
     */
    private String kickOutUrl;

    /**
     * 踢出之前登录/之后登录的用户。默认踢出之前登录的用户
     */
    private boolean kickOutAfter = false;

    /**
     * 同一个帐号最大会话数。默认1
     */
    private int maxSession = 1;

    private final ExpiredCache<String, Deque<Serializable>> cache;
    private final SessionManager sessionManager;

    public KickOutSessionControlFilter(ExpiredCacheManager cacheManager, SessionManager sessionManager) {
        this.cache = cacheManager.getCache(ShiroConstant.DEFAULT_KICK_OUT_CACHE);
        this.sessionManager = sessionManager;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        // 如果没有登录，直接进行之后的流程
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);
        if (!subject.isAuthenticated() && !subject.isRemembered()) {
            //如果没有登录，直接进行之后的流程
            return true;
        }

        Session session = subject.getSession();
        Serializable sessionId = session.getId();

        String username;
        if (subject.getPrincipal() instanceof String) {
            username = (String) subject.getPrincipal();
        } else if (subject.getPrincipal() instanceof UserPrincipal) {
            username = ((UserPrincipal) subject.getPrincipal()).getUsername();
        } else {
            throw new UnknownAccountException("获取账号失败");
        }

        // 强制同步
        synchronized (cache) {
            // 初始化用户的队列放到缓存里
            Deque<Serializable> deque = cache.get(username);
            if (deque == null) {
                deque = new LinkedList<>();
            }

            // 如果队列里没有此sessionId且用户没有被踢出，放入队列
            if (!deque.contains(sessionId) && session.getAttribute(ShiroConstant.DEFAULT_KICK_OUT_RETRY) == null) {
                session.setAttribute(ShiroConstant.DEFAULT_KICK_OUT_SUBJECT, username);
                deque.push(sessionId);
            }

            // 如果队列里的sessionId数超出最大会话数，开始踢人
            while (deque.size() > maxSession) {
                Serializable kickOutSessionId;

                if (kickOutAfter) {
                    // 踢出后者
                    kickOutSessionId = deque.removeFirst();
                } else {
                    // 踢出前者
                    kickOutSessionId = deque.removeLast();
                }
                try {
                    Session kickOutSession = sessionManager.getSession(new DefaultSessionKey(kickOutSessionId));
                    if (kickOutSession != null) {
                        // 设置会话的kick_out属性表示踢出了
                        kickOutSession.setAttribute(ShiroConstant.DEFAULT_KICK_OUT_RETRY, true);
                    }
                } catch (SessionException e) {
                    throw new UnknownSessionException("获取session失败", e);
                }
            }

            cache.put(username, deque);
        }

        // 如果被踢出了，直接退出，重定向到踢出后的地址
        if (session.getAttribute(ShiroConstant.DEFAULT_KICK_OUT_RETRY) != null) {
            // 会话被踢出了
            subject.logout();

            //如果是Ajax请求，不跳转登录
            if (WebUtil.isAjaxRequest(WebUtil.toHttp(request))) {
                HttpServletResponse rp = WebUtil.toHttp(response);
                rp.setCharacterEncoding("UTF-8");
                rp.setContentType("application/json;charset=utf-8");
                rp.setStatus(401);
            } else {
                saveRequest(request);
                WebUtils.issueRedirect(request, response, kickOutUrl);
            }
            return false;
        }
        return true;
    }

    public void setKickOutUrl(String kickOutUrl) {
        this.kickOutUrl = kickOutUrl;
    }

    public void setKickOutAfter(boolean kickOutAfter) {
        this.kickOutAfter = kickOutAfter;
    }

    public void setMaxSession(int maxSession) {
        this.maxSession = maxSession;
    }

}
