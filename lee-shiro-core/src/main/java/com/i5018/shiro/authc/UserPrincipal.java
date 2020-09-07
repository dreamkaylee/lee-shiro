package com.i5018.shiro.authc;

import com.i5018.shiro.constant.ShiroUserStateEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author limk
 * @date 2020/8/25 15:44
 */
@Data
public class UserPrincipal implements Serializable {

    /**
     * 用户唯一ID
     */
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 密钥
     */
    @JsonIgnore
    private String salt;

    /**
     * 用户状态
     */
    private Byte state;

    /**
     * 角色
     */
    private Set<String> roles;

    /**
     * 权限
     */
    private Set<String> permissions;

    public UserPrincipal(String username, String password, String salt, ShiroUserStateEnum state) {
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.state = state.getValue();
    }

    public UserPrincipal(Long id, String username, String password, String salt, ShiroUserStateEnum state) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.salt = salt;
        this.state = state.getValue();
    }

}
