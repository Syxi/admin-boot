package com.admin.module.system.service.impl;

import com.admin.common.constant.CacheConstants;
import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.common.security.SecurityConstants;
import com.admin.common.security.service.TokenService;
import com.admin.module.system.entity.SysMenu;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.entity.SysRoleMenu;
import com.admin.module.system.entity.SysUserRole;
import com.admin.module.system.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色缓存服务实现类
 * 统一管理角色权限缓存和在线用户Token刷新
 * 
 * @author suYan
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCacheServiceImpl implements RoleCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RoleCacheDataService roleCacheDataService;
    private final SysRoleMenuService roleMenuService;
    private final TokenService tokenService;

    /**
     * 应用启动时初始化所有角色权限缓存
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化角色权限缓存...");
        refreshAllRolePermsCache();
        log.info("角色权限缓存初始化完成");
    }

    /**
     * 刷新所有角色的权限缓存
     */
    @Override
    public void refreshAllRolePermsCache() {
        try {
            // 清空所有角色权限缓存
            Set<Object> cachedRoleCodes = redisTemplate.opsForHash().keys(CacheConstants.ROLE_PERMS_PREFIX);
            if (CollectionUtils.isNotEmpty(cachedRoleCodes)) {
                redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, cachedRoleCodes.toArray());
                log.debug("已清空 {} 个角色的权限缓存", cachedRoleCodes.size());
            }

            // 获取所有角色
            List<SysRole> roleList = roleCacheDataService.getAllActiveRoles();

            if (CollectionUtils.isEmpty(roleList)) {
                log.warn("系统中没有角色数据");
                return;
            }

            List<Long> roleIds = roleList.stream()
                    .map(SysRole::getRoleId)
                    .collect(Collectors.toList());

            // 获取所有角色菜单关系
            List<SysRoleMenu> roleMenuList = roleMenuService.selectRoleMenus(roleIds);
            
            // 按角色ID分组
            Map<Long, List<Long>> roleMenuMap = roleMenuList.stream()
                    .collect(Collectors.groupingBy(
                            SysRoleMenu::getRoleId,
                            Collectors.mapping(SysRoleMenu::getMenuId, Collectors.toList())
                    ));

            // 获取所有菜单ID
            Set<Long> allMenuIds = roleMenuList.stream()
                    .map(SysRoleMenu::getMenuId)
                    .collect(Collectors.toSet());

            // 批量查询菜单权限
            Map<Long, String> menuPermMap = getMenuPermMap(new ArrayList<>(allMenuIds));

            // 为每个角色构建权限缓存
            for (SysRole role : roleList) {
                List<Long> menuIds = roleMenuMap.getOrDefault(role.getRoleId(), Collections.emptyList());
                
                Set<String> perms = menuIds.stream()
                        .map(menuId -> menuPermMap.get(menuId))
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.toSet());

                // 存入Redis
                redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, role.getRoleCode(), perms);
            }

            log.info("成功刷新 {} 个角色的权限缓存", roleList.size());
        } catch (Exception e) {
            log.error("刷新所有角色权限缓存失败", e);
            throw new RuntimeException("刷新角色权限缓存失败", e);
        }
    }

    /**
     * 刷新指定角色的权限缓存
     */
    @Override
    public void refreshRolePermsCache(Long roleId) {
        try {
            SysRole role = roleCacheDataService.getRoleById(roleId);
            if (role == null) {
                log.warn("角色不存在: roleId={}", roleId);
                return;
            }

            refreshRolePermsCacheByCode(role.getRoleCode());
        } catch (Exception e) {
            log.error("刷新角色权限缓存失败: roleId={}", roleId, e);
            throw new RuntimeException("刷新角色权限缓存失败", e);
        }
    }

    /**
     * 刷新指定角色编码的权限缓存
     */
    @Override
    public void refreshRolePermsCacheByCode(String roleCode) {
        try {
            // 先清除旧缓存
            redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, roleCode);

            // 获取角色
            SysRole role = roleCacheDataService.getRoleByCode(roleCode);

            if (role == null) {
                log.warn("角色不存在: roleCode={}", roleCode);
                return;
            }

            // 获取角色的菜单ID列表
            List<Long> menuIds = roleMenuService.selectMenuIds(role.getRoleId());

            if (CollectionUtils.isEmpty(menuIds)) {
                // 没有菜单权限，存入空集合
                redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, roleCode, Collections.emptySet());
                log.debug("角色 {} 没有分配菜单权限", roleCode);
                return;
            }

            // 查询菜单权限
            List<SysMenu> menuList = roleCacheDataService.getMenusByIds(menuIds);
            menuList = menuList.stream()
                    .filter(menu -> StringUtils.isNotBlank(menu.getPerm()))
                    .collect(Collectors.toList());

            Set<String> perms = menuList.stream()
                    .map(SysMenu::getPerm)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toSet());

            // 更新缓存
            redisTemplate.opsForHash().put(CacheConstants.ROLE_PERMS_PREFIX, roleCode, perms);
            log.info("成功刷新角色权限缓存: roleCode={}, perms={}", roleCode, perms.size());

        } catch (Exception e) {
            log.error("刷新角色权限缓存失败: roleCode={}", roleCode, e);
            throw new RuntimeException("刷新角色权限缓存失败", e);
        }
    }

    /**
     * 批量刷新多个角色的权限缓存
     */
    @Override
    public void batchRefreshRolePermsCache(List<Long> roleIds) {
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }

        for (Long roleId : roleIds) {
            try {
                refreshRolePermsCache(roleId);
            } catch (Exception e) {
                log.error("批量刷新角色权限缓存失败: roleId={}", roleId, e);
            }
        }
    }

    /**
     * 清除角色权限缓存
     */
    @Override
    public void clearRolePermsCache(String roleCode) {
        redisTemplate.opsForHash().delete(CacheConstants.ROLE_PERMS_PREFIX, roleCode);
        log.info("清除角色权限缓存: roleCode={}", roleCode);
    }

    /**
     * 获取角色权限（从缓存）
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getRolePermsFromCache(String roleCode) {
        Object perms = redisTemplate.opsForHash().get(CacheConstants.ROLE_PERMS_PREFIX, roleCode);
        if (perms instanceof Set) {
            return (Set<String>) perms;
        }
        return Collections.emptySet();
    }

    /**
     * 使拥有指定角色的所有在线用户Token失效
     * 当角色权限变更时调用，强制用户重新登录以获取最新权限
     */
    @Override
    public void invalidateOnlineUsersByRole(String roleCode) {
        try {
            // 查找拥有该角色的所有用户
            SysRole role = roleCacheDataService.getRoleByCode(roleCode);

            if (role == null) {
                log.warn("角色不存在: roleCode={}", roleCode);
                return;
            }

            // 查询拥有该角色的用户ID列表
            List<SysUserRole> userRoles = roleCacheDataService.getUserRolesByRoleId(role.getRoleId());

            if (CollectionUtils.isEmpty(userRoles)) {
                log.debug("没有用户拥有角色: roleCode={}", roleCode);
                return;
            }

            // 获取所有在线用户
            Set<String> onlineUserKeys = redisTemplate.keys(SecurityConstants.ONLINE_USER_PREFIX + "*");
            if (CollectionUtils.isEmpty(onlineUserKeys)) {
                log.debug("当前没有在线用户");
                return;
            }

            int invalidatedCount = 0;
            for (String key : onlineUserKeys) {
                try {
                    Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
                    if (userData.isEmpty()) {
                        continue;
                    }

                    String token = (String) userData.get("token");
                    Long userId = (Long) userData.get("userId");

                    // 检查该用户是否拥有被修改的角色
                    boolean hasRole = userRoles.stream()
                            .anyMatch(ur -> ur.getUserId().equals(userId));

                    if (hasRole && token != null) {
                        // 将Token加入黑名单
                        tokenService.blacklistToken(token);
                        invalidatedCount++;
                        log.debug("已使Token失效: userId={}, roleCode={}", userId, roleCode);
                    }
                } catch (Exception e) {
                    log.error("处理在线用户Token失败: key={}", key, e);
                }
            }

            log.info("角色权限变更，已使 {} 个在线用户Token失效: roleCode={}", invalidatedCount, roleCode);

        } catch (Exception e) {
            log.error("使在线用户Token失效失败: roleCode={}", roleCode, e);
        }
    }

    /**
     * 使指定用户的缓存失效（用户角色变更时调用）
     */
    @Override
    public void invalidateUserCache(String username) {
        try {
            String key = SecurityConstants.ONLINE_USER_PREFIX + username;
            Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);

            if (userData.isEmpty()) {
                log.debug("用户不在线: username={}", username);
                return;
            }

            String token = (String) userData.get("token");
            if (token != null) {
                // 将Token加入黑名单
                tokenService.blacklistToken(token);
                log.info("用户角色变更，已使Token失效: username={}", username);
            }

        } catch (Exception e) {
            log.error("使用户缓存失效失败: username={}", username, e);
        }
    }

    @Override
    public void invalidateTenantUserCache(Long tenantId) {
        try {
            if (tenantId == null) {
                log.warn("租户ID为空，无法清除租户用户缓存");
                return;
            }

            // 获取所有在线用户
            Set<String> onlineUserKeys = redisTemplate.keys(SecurityConstants.ONLINE_USER_PREFIX + "*");
            if (CollectionUtils.isEmpty(onlineUserKeys)) {
                log.debug("当前没有在线用户");
                return;
            }

            int invalidatedCount = 0;
            for (String key : onlineUserKeys) {
                try {
                    Map<Object, Object> userData = redisTemplate.opsForHash().entries(key);
                    if (userData.isEmpty()) {
                        continue;
                    }

                    String token = (String) userData.get("token");
                    Long userId = (Long) userData.get("userId");

                    // 检查该用户是否属于指定租户
                    Long userTenantId = (Long) userData.get("tenantId");

                    if (userTenantId != null && userTenantId.equals(tenantId) && token != null) {
                        // 将Token加入黑名单
                        tokenService.blacklistToken(token);
                        invalidatedCount++;
                        log.debug("已使租户用户Token失效: userId={}, tenantId={}", userId, tenantId);
                    }
                } catch (Exception e) {
                    log.error("处理在线租户用户Token失败: key={}", key, e);
                }
            }

            log.info("租户套餐变更，已使 {} 个租户用户Token失效: tenantId={}", invalidatedCount, tenantId);

        } catch (Exception e) {
            log.error("使租户用户缓存失效失败: tenantId={}", tenantId, e);
        }
    }

    /**
     * 获取菜单权限映射
     */
    private Map<Long, String> getMenuPermMap(List<Long> menuIds) {
        if (CollectionUtils.isEmpty(menuIds)) {
            return Collections.emptyMap();
        }

        List<SysMenu> menuList = roleCacheDataService.getMenusByIds(menuIds);

        return menuList.stream()
                .filter(menu -> StringUtils.isNotBlank(menu.getPerm()))
                .collect(Collectors.toMap(SysMenu::getMenuId, SysMenu::getPerm, (v1, v2) -> v1));
    }
}
