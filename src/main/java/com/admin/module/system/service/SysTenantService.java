package com.admin.module.system.service;

import com.admin.module.system.dto.TenantUserForm;
import com.admin.module.system.entity.SysTenant;
import com.admin.module.system.query.TenantQuery;
import com.admin.module.system.vo.TransferVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
* @author 70993
* @description 针对表【sys_tenant(租户表)】的数据库操作Service
* @createDate 2025-11-19 11:02:56
*/
public interface SysTenantService extends IService<SysTenant> {

    /**
     * 租户分页列表
     * @param tenantQuery
     * @return
     */
    IPage<SysTenant> selectUserPage(TenantQuery tenantQuery);

    /**
     * 新增租户
     * @param tenant
     * @return
     */
    boolean saveTenant(SysTenant tenant);

    /**
     * 更新租户
     * @param tenant
     * @return
     */
    boolean updateTenant(SysTenant tenant);

    /**
     * 获取租户详情
     * @param id
     * @return
     */
    SysTenant getTenantDetail(@NotNull(message = "id不能为空") Long id);

    /**
     * 批量删除租户
     * @param ids
     * @return
     */
    boolean deleteBatchTenants(List<Long> ids);

    /**
     * 给租户分配用户
     * @param userIds
     * @param tenantId
     * @return
     */
    boolean updateTenantUsers(List<Long> userIds, Long tenantId);



    /**
     * 获取用户可访问的租户列表
     * @return
     */
    List<TenantUserForm> getUserTenants();

    /**
     * 检查用户是否有权访问指定租户
     * @param userId 用户ID
     * @param tenantId 租户ID
     * @return
     */
    boolean checkUserTenantAccess(Long userId, Long tenantId);


    /**
     * 分页获取未分配租户的用户（支持关键字搜索）
     */
    IPage<TransferVO> selectUsersNotInTenantPage(Long tenantId, Integer pageNum, Integer pageSize, String keyword);


    /**
     * 分页获取租户下的用户（支持关键字搜索）
     */
    IPage<TransferVO> selectUsersInTenantPage(Long tenantId, Integer pageNum, Integer pageSize, String keyword);


}
