package com.admin.module.system.service.impl;

import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.common.exception.CustomException;
import com.admin.common.security.SecurityUtils;
import com.admin.module.system.dto.TenantUserForm;
import com.admin.module.system.entity.SysTenant;
import com.admin.module.system.entity.SysTenantUser;
import com.admin.module.system.entity.SysUser;
import com.admin.module.system.mapper.SysTenantMapper;
import com.admin.module.system.query.TenantQuery;
import com.admin.module.system.service.SysTenantService;
import com.admin.module.system.service.SysTenantUserService;
import com.admin.module.system.service.SysUserService;
import com.admin.module.system.vo.TransferVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 70993
* @description 针对表【sys_tenant(租户表)】的数据库操作Service实现
* @createDate 2025-11-19 11:02:56
*/
@RequiredArgsConstructor
@Service
public class SysTenantServiceImpl extends ServiceImpl<SysTenantMapper, SysTenant>
    implements SysTenantService{

    private final SysTenantUserService sysTenantUserService;

    private final SysUserService sysUserService;

    /**
     * 租户分页列表
     *
     * @param tenantQuery
     * @return
     */
    @Override
    public IPage<SysTenant> selectUserPage(TenantQuery tenantQuery) {
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(tenantQuery.getName())) {
            queryWrapper.like(SysTenant::getName, tenantQuery.getName());
        }
        if (StringUtils.isNotEmpty(tenantQuery.getCode())) {
            queryWrapper.like(SysTenant::getCode, tenantQuery.getCode());
        }
        
        // 非admin用户只能看到自己有权访问的租户
        if (!SecurityUtils.isAdmin()) {
            // 获取当前用户ID
            Long userId = SecurityUtils.getUserId();
            
            // 查询用户关联的租户ID列表
            LambdaQueryWrapper<SysTenantUser> tenantUserQuery = new LambdaQueryWrapper<>();
            tenantUserQuery.eq(SysTenantUser::getUserId, userId);
            List<SysTenantUser> tenantUserList = sysTenantUserService.list(tenantUserQuery);
            
            if (!tenantUserList.isEmpty()) {
                // 获取租户ID列表
                List<Long> tenantIds = tenantUserList.stream()
                        .map(SysTenantUser::getTenantId)
                        .collect(Collectors.toList());
                
                // 只查询用户有权访问的租户
                queryWrapper.in(SysTenant::getId, tenantIds);
            } else {
                // 如果用户没有关联任何租户，返回空结果
                queryWrapper.eq(SysTenant::getId, -1L); // 使用不存在的ID来返回空结果
            }
        }
        
        queryWrapper.orderByAsc(SysTenant::getSort);
        Page<SysTenant> page = new Page<>(tenantQuery.getPage(), tenantQuery.getLimit());
        return this.page(page, queryWrapper);
    }

    /**
     * 新增租户
     *
     * @param tenant
     * @return
     */
    @Override
    public boolean saveTenant(SysTenant tenant) {
        if (exitsTenant(tenant.getName(), null)) {
            throw new CustomException("租户名称已存在");
        }
        return this.save(tenant);
    }

    /**
     * 更新租户
     *
     * @param tenant
     * @return
     */
    @Override
    public boolean updateTenant(SysTenant tenant) {
        if (exitsTenant(tenant.getName(), tenant.getId())) {
            throw new CustomException("租户名称已存在");
        }
        return this.updateById(tenant);
    }

    /**
     * 判断租户是否存在
     *
     */
    private boolean exitsTenant(String name, Long id) {
        LambdaQueryWrapper<SysTenant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenant::getName, name);
        queryWrapper.eq(SysTenant::getDeleted, DeletedEnum.NO_DELETE.getValue());
        if (id != null) {
            queryWrapper.ne(SysTenant::getId, id);
        }
        boolean result = this.exists(queryWrapper);
        return result;
    }

    /**
     * 获取租户详情
     *
     * @param id
     * @return
     */
    @Override
    public SysTenant getTenantDetail(Long id) {
        return this.getById(id);
    }

    /**
     * 批量删除租户
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatchTenants(List<Long> ids) {
        boolean result = this.removeBatchByIds(ids);

        if (result) {
            // 同时将属于这些租户的用户租户ID设为null
            LambdaUpdateWrapper<SysUser> userUpdateWrapper = new LambdaUpdateWrapper<>();
            userUpdateWrapper.in(SysUser::getTenantId, ids)
                           .set(SysUser::getTenantId, null);
            sysUserService.update(userUpdateWrapper);
        }
        return result;
    }

    /**
     * 给租户分配用户
     *
     * @param userIds
     * @param tenantId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTenantUsers(List<Long> userIds, Long tenantId) {
        if (userIds == null || userIds.isEmpty()) {
            // 如果没有用户提供，先清除该租户的所有用户关联
            LambdaUpdateWrapper<SysUser> clearUpdateWrapper = new LambdaUpdateWrapper<>();
            clearUpdateWrapper.eq(SysUser::getTenantId, tenantId)
                             .set(SysUser::getTenantId, null);
            sysUserService.update(clearUpdateWrapper);
            return true;
        }
        
        // 先清除该租户的所有用户关联
        LambdaUpdateWrapper<SysUser> clearUpdateWrapper = new LambdaUpdateWrapper<>();
        clearUpdateWrapper.eq(SysUser::getTenantId, tenantId)
                         .set(SysUser::getTenantId, null);
        sysUserService.update(clearUpdateWrapper);
        
        // 再为提供的用户设置租户ID
        LambdaUpdateWrapper<SysUser> userUpdateWrapper = new LambdaUpdateWrapper<>();
        userUpdateWrapper.in(SysUser::getUserId, userIds)
                       .set(SysUser::getTenantId, tenantId);
        return sysUserService.update(userUpdateWrapper);
    }



    private TransferVO convertToTransferVO(SysUser user) {
        TransferVO transferVO = new TransferVO();
        transferVO.setKey(user.getUserId());
        transferVO.setLabel(user.getUsername());
        transferVO.setRealName(user.getRealName());
        return transferVO;
    }

    @Override
    public List<TenantUserForm> getUserTenants() {
        // 获取当前用户ID
        Long userId = com.admin.common.security.SecurityUtils.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 直接查询当前用户的信息以获取其租户ID
        SysUser currentUser = sysUserService.getById(userId);
        if (currentUser == null || currentUser.getTenantId() == null) {
            return Collections.emptyList();
        }
        
        // 查询用户所属租户的信息
        SysTenant tenant = this.getById(currentUser.getTenantId());
        if (tenant == null) {
            return Collections.emptyList();
        }
        
        // 转换为TenantUserForm列表
        TenantUserForm form = new TenantUserForm();
        form.setTenantId(tenant.getId());
        form.setTenantName(tenant.getName());
        form.setUserId(userId);
        return Collections.singletonList(form);
    }

    @Override
    public boolean checkUserTenantAccess(Long userId, Long tenantId) {
        // 查询用户是否属于指定租户（通过tenant_id字段判断）
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUserId, userId)
                .eq(SysUser::getTenantId, tenantId);
        
        return sysUserService.count(queryWrapper) > 0;
    }

    @Override
    public IPage<TransferVO> selectUsersNotInTenantPage(Long tenantId, Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        // 查询租户ID不等于指定租户ID的用户，或者租户ID为null的用户
        queryWrapper.and(wrapper ->
            wrapper.ne(SysUser::getTenantId, tenantId).or().isNull(SysUser::getTenantId)
        );

        // 添加关键词搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }

        queryWrapper.orderByAsc(SysUser::getSort);

        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserService.page(page, queryWrapper);

        // 转换为TransferVO分页对象
        return userPage.convert(this::convertToTransferVO);
    }


    @Override
    public IPage<TransferVO> selectUsersInTenantPage(Long tenantId, Integer pageNum, Integer pageSize, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getTenantId, tenantId)  // 根据tenant_id字段查询
               .eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue())
               .eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());

        // 添加关键词搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }

        wrapper.orderByAsc(SysUser::getSort);

        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserService.page(page, wrapper);

        // 转换为TransferVO分页对象
        return userPage.convert(this::convertToTransferVO);
    }

}




