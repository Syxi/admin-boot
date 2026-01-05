package com.admin.module.system.service.impl;

import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.StatusEnum;
import com.admin.common.exception.CustomException;
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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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

        // 删除租户用户关联数据
        if (result) {
            sysTenantUserService.removeByTenantIds(ids);
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
    public boolean updateTenantUsers(List<Long> userIds, Long tenantId) {
        LambdaQueryWrapper<SysTenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantUser::getTenantId, tenantId);
        List<SysTenantUser> tenantUserList = sysTenantUserService.list(queryWrapper);
        sysTenantUserService.removeBatchByIds(tenantUserList);

        List<SysTenantUser> sysTenantUserList = userIds.stream()
                .map(userId -> new SysTenantUser(userId, tenantId))
                .toList();
        sysTenantUserService.saveBatch(sysTenantUserList);
        return true;
    }

    /**
     * 获取租户下的用户
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<TransferVO> selectUsersInTenant(Long tenantId) {
        List<Long> userIds = sysTenantUserService.selectUserIdsByTenantId(tenantId);
        if (CollectionUtils.isNotEmpty(userIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getUserId, userIds);
        wrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        wrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        wrapper.orderByAsc(SysUser::getSort);
        List<SysUser> userList = sysUserService.list(wrapper);
        List<TransferVO> transferVOList = userList.stream()
                .map(this::convertToTransferVO)
                .collect(toList());
        return transferVOList;
    }

    /**
     * 获取未分配租户的用户
     *
     * @param tenantId
     * @return
     */
    @Override
    public List<TransferVO> selectUsersNotInTenant(Long tenantId) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        queryWrapper.orderByAsc(SysUser::getSort);

        List<TransferVO> transferVOS = sysUserService.list(queryWrapper)
                .stream()
                .map(this::convertToTransferVO)
                .collect(Collectors.toList());
        return transferVOS;
    }

    private TransferVO convertToTransferVO(SysUser user) {
        TransferVO transferVO = new TransferVO();
        transferVO.setKey(user.getUserId());
        transferVO.setLabel(user.getUsername());
        return transferVO;
    }

    @Override
    public List<TenantUserForm> getUserTenants() {
        // 获取当前用户ID
        Long userId = com.admin.common.security.SecurityUtils.getUserId();
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 查询用户关联的租户列表
        LambdaQueryWrapper<SysTenantUser> tenantUserQuery = new LambdaQueryWrapper<>();
        tenantUserQuery.eq(SysTenantUser::getUserId, userId);
        List<SysTenantUser> tenantUserList = sysTenantUserService.list(tenantUserQuery);
        
        if (CollectionUtils.isEmpty(tenantUserList)) {
            return Collections.emptyList();
        }
        
        // 获取租户ID列表
        List<Long> tenantIds = tenantUserList.stream()
                .map(SysTenantUser::getTenantId)
                .collect(Collectors.toList());
        
        // 查询租户信息
        LambdaQueryWrapper<SysTenant> tenantQuery = new LambdaQueryWrapper<>();
        tenantQuery.in(SysTenant::getId, tenantIds);
        List<SysTenant> tenantList = this.list(tenantQuery);
        
        // 转换为TenantUserForm列表
        return tenantList.stream().map(tenant -> {
            TenantUserForm form = new TenantUserForm();
            form.setTenantId(tenant.getId());
            form.setTenantName(tenant.getName());
            form.setUserId(userId);
            return form;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean checkUserTenantAccess(Long userId, Long tenantId) {
        // 查询用户是否关联到指定租户
        LambdaQueryWrapper<SysTenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantUser::getUserId, userId)
                .eq(SysTenantUser::getTenantId, tenantId);
        
        return sysTenantUserService.count(queryWrapper) > 0;
    }
}




