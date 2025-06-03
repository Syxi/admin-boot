package com.admin.common.security;

import com.admin.common.constant.SystemConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.PatternMatchUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 获取当前登录人信息
 * @author suYan
 * @date 2023/4/8 22:45
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户信息
     * @return
     */
    public static Optional<SysUserDetails> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof SysUserDetails) {
                return Optional.of((SysUserDetails) principal);
            }
        }
        return Optional.empty();
    }


    /**
     * 获取用户id
     * @return
     */
    public static Long getUserId() {
        return getCurrentUser().map(SysUserDetails::getUserId).orElse(null);
    }


    /**
     * 获取用户名
     * @return
     */
    public static String getUserName() {
        return getCurrentUser().map(SysUserDetails::getUsername).orElse(null);
    }


    /**
     * 是否超级管理员
     * @return
     */
    public static boolean isAdmin() {
        Set<String> roles = getRoleCodes();
        if (roles.contains(SystemConstants.ADMIN_ROLE_CODE)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 获取组织id
     * @return
     */
    public static Long getOrganId() {
        return getCurrentUser().map(SysUserDetails::getOrganId).orElse(null);
    }


    /**
     * 获取数据权限范围
     * @return
     */
    public static Integer getDataScope() {
        return getCurrentUser().map(SysUserDetails::getDataScope).orElse(null);
    }

    /**
     * 获取用户角色集合
     * @return
     */
    public static Set<String> getRoleCodes() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (CollectionUtils.isNotEmpty(authorities)) {
                Set<String> roles = authorities.stream().filter(item -> item.getAuthority().startsWith("ROLE_"))
                        .map(item -> StringUtils.removeStart(item.getAuthority(), "ROLE_"))
                        .collect(Collectors.toSet());
                return roles;
            }
        }
        return Collections.EMPTY_SET;
    }


    /**
     * 获取用户权限集合
     * @return
     */
    public static Set<String> getPermissons() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (CollectionUtils.isNotEmpty(authorities)) {
                Set<String> permissions = authorities.stream().filter(item -> item.getAuthority().startsWith("ROLE_"))
                        .map(item -> item.getAuthority())
                        .collect(Collectors.toSet());
                return permissions;
            }
        }
        return Collections.EMPTY_SET;
    }


    /**
     *  是否拥有权限判断
     * @param permission
     * @return
     */
    public static boolean hasPermission(String permission) {
        Set<String> permissions = getPermissons();
        boolean hasPermission = permissions.stream().anyMatch(item -> PatternMatchUtils.simpleMatch(permission, item));
        return hasPermission;
    }



}
