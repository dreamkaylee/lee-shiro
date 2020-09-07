package com.i5018.shiro.session;

/**
 * 由于SimpleSession lastAccessTime更改后也会调用SessionDao update方法，
 * 增加标识位，如果只是更新lastAccessTime SessionDao update方法直接返回
 * <p>
 * Session Attribute
 * DefaultSubjectContext.PRINCIPALS_SESSION_KEY 保存 principal
 * DefaultSubjectContext.AUTHENTICATED_SESSION_KEY 保存 boolean是否登陆
 *
 * @author limk
 * @date 2020/9/1 21:44
 * @see org.apache.shiro.subject.support.DefaultSubjectContext
 */
public class ShiroSession extends AbstractSession {

    public ShiroSession() {
        super();
        this.setChanged(true);
    }

    public ShiroSession(String host) {
        super(host);
        this.setChanged(true);
    }

}
