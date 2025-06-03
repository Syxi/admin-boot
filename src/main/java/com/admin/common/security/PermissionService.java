package com.admin.common.security;

import com.admin.common.constant.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;

import java.util.*;

/**
 * 用户登录时，验证用户是否拥有某个节点权限 例：（sys:user:add）
 * @author suYan
 * @date 2023/4/2 14:44
 */

@Component("pms")
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final RedisTemplate redisTemplate;


    /**
     * 判断当前用户是否拥有操作权限
     *
     * @param requirePerm
     * @return
     */
    public boolean hasPerm(String requirePerm) {
        // 超级管理员放行
        if (SecurityUtils.isAdmin()) {
            return true;
        }

        if (StringUtils.isBlank(requirePerm)) {
            return false;
        }

        // 获取档案登录用户的角色编码集合
        Set<String> roleCodes = SecurityUtils.getRoleCodes();
        if (CollectionUtils.isEmpty(roleCodes)) {
            return false;
        }


        // 获取当前登录用户的角色编码集合
        Set<String> rolePerms = this.getRolePermsFromCache(roleCodes);
        if (CollectionUtils.isEmpty(rolePerms)) {
            return false;
        }

        // 判断当前用户的所有角色的权限列表中是否包含所需的权限
        boolean hasPermission = rolePerms.stream()
                .anyMatch(rolePerm -> PatternMatchUtils.simpleMatch(rolePerm, requirePerm));

        if (!hasPermission) {
            log.error("用户没有访问权限");
        }


        return hasPermission;


    }




    /**
     * 从缓存中获取角色权限列表
     * @param roleCodes
     * @return
     */
    public Set<String> getRolePermsFromCache(Set<String> roleCodes) {
        if (CollectionUtils.isEmpty(roleCodes)) {
            return Collections.emptySet();
        }

        Set<String> perms = new HashSet<>();
        // 从缓存中一次性获取所有角色的权限
        Collection<Object> roleCodeAsObjects = new ArrayList<>(roleCodes);
        List<Object> rolePermList = redisTemplate.opsForHash().multiGet(CacheConstants.ROLE_PERMS_PREFIX, roleCodeAsObjects);

        for (Object rolePerm : rolePermList) {
            if (rolePerm instanceof Set) {
                @SuppressWarnings("unchecked")
                Set<String> rolePerms = (Set<String>) rolePerm;
                perms.addAll(rolePerms);
            }
        }

        return perms;
    }













}