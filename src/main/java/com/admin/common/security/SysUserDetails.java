package com.admin.common.security;

import com.admin.common.enums.StatusEnum;
import com.admin.module.system.dto.UserAuthInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 封装 MyUserDetailsServiceImpl查询的用户信息
 * @author suYan
 * @date 2023/4/5 16:01
 */
@Data
@NoArgsConstructor
public class SysUserDetails implements UserDetails {

    @Getter
    private Long userId;

    private String username;

    private String password;

    private Long deptId;

    private Integer dataScope;

    @Schema(description = "账号是否可用")
    private Boolean enabled;

    @Schema(description = "角色集合")
    private Collection<SimpleGrantedAuthority> authorities;

    private Set<String> permissions;





    /**
     * 接收从 UserDetailsServiceImpl中的用户信息 MyUserDetails
     * @param userAuthInfo
     */
    public SysUserDetails(UserAuthInfo userAuthInfo) {
        this.userId = userAuthInfo.getUserId();
        Set<String> roles = userAuthInfo.getRoles();
        Set<SimpleGrantedAuthority> authorities;
        if (CollectionUtils.isNotEmpty(roles)) {
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toSet());
        } else {
            authorities = Collections.emptySet();
        }
        this.authorities = authorities;
        this.username = userAuthInfo.getUsername();
        this.password = userAuthInfo.getPassword();
        this.deptId = userAuthInfo.getDeptId();
        this.dataScope = userAuthInfo.getDataScope();
        this.permissions = userAuthInfo.getPermissions();
        this.enabled = ObjectUtils.nullSafeEquals(userAuthInfo.getStatus(), StatusEnum.ENABLE.getValue());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 账号是否过期
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否被锁定
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 密码是否过期
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否可用
     * @return
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
