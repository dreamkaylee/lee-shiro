package com.i5018.shiro.constant;

import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * @author limk
 * @date 2020/8/25 15:48
 */
public enum ShiroUserStateEnum implements Serializable {

    /**
     * 正常
     */
    NORMAL(Byte.valueOf("0")),

    /**
     * 锁定
     */
    LOCKED(Byte.valueOf("1")),

    /**
     * 禁用
     */
    DISABLED(Byte.valueOf("2")),

    /**
     * 过期
     */
    EXPIRED(Byte.valueOf("3"));

    ShiroUserStateEnum(Byte value) {
        this.value = value;
    }

    private final Byte value;

    @JsonValue
    public Byte getValue() {
        return value;
    }

}
