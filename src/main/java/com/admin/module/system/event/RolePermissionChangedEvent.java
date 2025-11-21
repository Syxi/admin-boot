package com.admin.module.system.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 角色权限变更事件
 * 当角色的权限发生变化时发布此事件
 * 
 * @author suYan
 */
@Getter
public class RolePermissionChangedEvent extends ApplicationEvent {

    /**
     * 变更类型
     */
    private final ChangeType changeType;
    
    /**
     * 角色ID（单个角色变更时使用）
     */
    private final Long roleId;
    
    /**
     * 角色编码（单个角色变更时使用）
     */
    private final String roleCode;
    
    /**
     * 角色ID列表（批量角色变更时使用）
     */
    private final List<Long> roleIds;

    /**
     * 构造函数 - 单个角色变更
     */
    public RolePermissionChangedEvent(Object source, ChangeType changeType, Long roleId, String roleCode) {
        super(source);
        this.changeType = changeType;
        this.roleId = roleId;
        this.roleCode = roleCode;
        this.roleIds = null;
    }

    /**
     * 构造函数 - 批量角色变更
     */
    public RolePermissionChangedEvent(Object source, ChangeType changeType, List<Long> roleIds) {
        super(source);
        this.changeType = changeType;
        this.roleId = null;
        this.roleCode = null;
        this.roleIds = roleIds;
    }

    /**
     * 构造函数 - 全局刷新
     */
    public RolePermissionChangedEvent(Object source, ChangeType changeType) {
        super(source);
        this.changeType = changeType;
        this.roleId = null;
        this.roleCode = null;
        this.roleIds = null;
    }

    /**
     * 变更类型枚举
     */
    public enum ChangeType {
        /** 角色菜单变更 */
        ROLE_MENU_UPDATED,
        
        /** 菜单权限变更 */
        MENU_PERMISSION_UPDATED,
        
        /** 菜单删除 */
        MENU_DELETED,
        
        /** 角色编码变更 */
        ROLE_CODE_CHANGED,
        
        /** 角色删除 */
        ROLE_DELETED,
        
        /** 全局刷新 */
        REFRESH_ALL
    }
}
