package com.i5018.shiro.authc.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @author limk
 * @date 2020/8/25 17:16
 */
public class SmsToken implements AuthenticationToken {

    private String sms;

    public SmsToken(final String sms) {
        this.sms = sms;
    }

    @Override
    public Object getPrincipal() {
        return this.sms;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
    }

}
