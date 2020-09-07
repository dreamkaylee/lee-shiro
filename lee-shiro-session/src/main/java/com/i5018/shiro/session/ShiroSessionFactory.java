package com.i5018.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionFactory;

/**
 * @author limk
 * @date 2020/9/1 22:01
 */
public class ShiroSessionFactory implements SessionFactory {

    @Override
    public Session createSession(SessionContext initData) {
        if (initData != null) {
            String host = initData.getHost();
            if (host != null) {
                return new ShiroSession(host);
            }
        }

        return new ShiroSession();
    }

}
