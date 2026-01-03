package com.admin.common.security;

import com.admin.module.system.entity.SysTenantPackage;
import com.admin.module.system.service.SysTenantConfigService;
import com.admin.module.system.service.SysTenantPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 租户安全工具类
 * 用于处理租户级别的权限和资源限制
 */
@Component
@Slf4j
public class TenantSecurityUtils {

    @Autowired
    private SysTenantConfigService sysTenantConfigService;

    @Autowired
    private SysTenantPackageService sysTenantPackageService;

    /**
     * 检查租户用户数是否超出限制
     * @param tenantId 租户ID
     * @return true-未超出限制，false-超出限制
     */
    public boolean checkUserLimit(Long tenantId) {
        try {
            // 获取租户配置信息
            var tenantConfig = sysTenantConfigService.getTenantConfig(tenantId);
            if (tenantConfig == null) {
                // 如果没有配置，默认允许操作
                return true;
            }

            // 获取套餐的最大用户数
            var maxUsers = tenantConfig.getPackageId() != null ? 
                getPackageMaxUsers(tenantConfig.getPackageId()) : Integer.MAX_VALUE;

            // 检查当前用户数是否超出限制
            var currentUsers = tenantConfig.getCurrentUsers() != null ? 
                tenantConfig.getCurrentUsers() : 0;

            return currentUsers < maxUsers;
        } catch (Exception e) {
            log.error("检查租户用户限制时出错: ", e);
            return true; // 发生错误时，默认允许操作
        }
    }

    /**
     * 检查租户存储空间是否超出限制
     * @param tenantId 租户ID
     * @return true-未超出限制，false-超出限制
     */
    public boolean checkStorageLimit(Long tenantId) {
        try {
            var tenantConfig = sysTenantConfigService.getTenantConfig(tenantId);
            if (tenantConfig == null) {
                return true;
            }

            // 获取套餐的最大存储空间
            var maxStorage = tenantConfig.getPackageId() != null ? 
                getPackageMaxStorage(tenantConfig.getPackageId()) : Long.MAX_VALUE;

            // 检查当前存储使用量是否超出限制
            var currentStorage = tenantConfig.getCurrentStorage() != null ? 
                tenantConfig.getCurrentStorage() : 0L;

            return currentStorage < maxStorage;
        } catch (Exception e) {
            log.error("检查租户存储限制时出错: ", e);
            return true;
        }
    }

    /**
     * 检查租户是否过期
     * @param tenantId 租户ID
     * @return true-未过期，false-已过期
     */
    public boolean checkTenantValidity(Long tenantId) {
        try {
            var tenantConfig = sysTenantConfigService.getTenantConfig(tenantId);
            if (tenantConfig == null) {
                // 没有配置信息，视为有效
                return true;
            }

            // 检查套餐是否过期
            var endTime = tenantConfig.getEndTime();
            if (endTime == null) {
                // 没有设置过期时间，视为有效
                return true;
            }

            return java.time.LocalDateTime.now().isBefore(endTime);
        } catch (Exception e) {
            log.error("检查租户有效性时出错: ", e);
            return true;
        }
    }

    /**
     * 获取套餐最大用户数
     * @param packageId 套餐ID
     * @return 最大用户数
     */
    private Integer getPackageMaxUsers(Long packageId) {
        try {
            SysTenantPackage tenantPackage = sysTenantPackageService.getById(packageId);
            return tenantPackage != null ? tenantPackage.getMaxUsers() : Integer.MAX_VALUE;
        } catch (Exception e) {
            log.error("获取套餐最大用户数时出错: ", e);
            return Integer.MAX_VALUE; // 发生错误时返回最大值，避免影响正常使用
        }
    }

    /**
     * 获取套餐最大存储空间
     * @param packageId 套餐ID
     * @return 最大存储空间(MB)
     */
    private Long getPackageMaxStorage(Long packageId) {
        try {
            SysTenantPackage tenantPackage = sysTenantPackageService.getById(packageId);
            return tenantPackage != null ? tenantPackage.getMaxStorage() : Long.MAX_VALUE;
        } catch (Exception e) {
            log.error("获取套餐最大存储空间时出错: ", e);
            return Long.MAX_VALUE; // 发生错误时返回最大值，避免影响正常使用
        }
    }
}