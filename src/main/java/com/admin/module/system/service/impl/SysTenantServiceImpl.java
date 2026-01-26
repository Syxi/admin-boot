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
        if (CollectionUtils.isEmpty(userIds)) {
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
        // 获取已在租户中的用户ID列表
        List<Long> userIdsInTenant = sysTenantUserService.selectUserIdsByTenantId(tenantId);
        
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        // 如果已有用户在租户中，则排除这些用户
        if (CollectionUtils.isNotEmpty(userIdsInTenant)) {
            queryWrapper.notIn(SysUser::getUserId, userIdsInTenant);
        }
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

    @Override
    public IPage<TransferVO> selectUsersNotInTenantPage(Long tenantId, Integer pageNum, Integer pageSize) {
        // 获取已在租户中的用户ID列表
        List<Long> userIdsInTenant = sysTenantUserService.selectUserIdsByTenantId(tenantId);
        
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        // 如果已有用户在租户中，则排除这些用户
        if (CollectionUtils.isNotEmpty(userIdsInTenant)) {
            queryWrapper.notIn(SysUser::getUserId, userIdsInTenant);
        }
        queryWrapper.orderByAsc(SysUser::getSort);

        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserService.page(page, queryWrapper);
        
        // 转换为TransferVO分页对象
        List<TransferVO> transferVOList = userPage.getRecords().stream()
                .map(this::convertToTransferVO)
                .collect(Collectors.toList());
        
        IPage<TransferVO> transferVOPage = new Page<>();
        transferVOPage.setRecords(transferVOList);
        transferVOPage.setCurrent(userPage.getCurrent());
        transferVOPage.setSize(userPage.getSize());
        transferVOPage.setTotal(userPage.getTotal());
        
        return transferVOPage;
    }

    @Override
    public IPage<TransferVO> selectUsersNotInTenantPage(Long tenantId, Integer pageNum, Integer pageSize, String keyword) {
        // 获取已在租户中的用户ID列表
        List<Long> userIdsInTenant = sysTenantUserService.selectUserIdsByTenantId(tenantId);
        
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        queryWrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        // 如果已有用户在租户中，则排除这些用户
        if (CollectionUtils.isNotEmpty(userIdsInTenant)) {
            queryWrapper.notIn(SysUser::getUserId, userIdsInTenant);
        }
        
        // 添加关键词搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }
        
        queryWrapper.orderByAsc(SysUser::getSort);

        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserService.page(page, queryWrapper);
        
        // 转换为TransferVO分页对象
        List<TransferVO> transferVOList = userPage.getRecords().stream()
                .map(this::convertToTransferVO)
                .collect(Collectors.toList());
        
        IPage<TransferVO> transferVOPage = new Page<>();
        transferVOPage.setRecords(transferVOList);
        transferVOPage.setCurrent(userPage.getCurrent());
        transferVOPage.setSize(userPage.getSize());
        transferVOPage.setTotal(userPage.getTotal());
        
        return transferVOPage;
    }

    @Override
    public IPage<TransferVO> selectUsersInTenantPage(Long tenantId, Integer pageNum, Integer pageSize) {
        List<Long> userIds = sysTenantUserService.selectUserIdsByTenantId(tenantId);
        if (CollectionUtils.isEmpty(userIds)) {
            // 返回空分页对象
            IPage<TransferVO> emptyPage = new Page<>();
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setCurrent(pageNum);
            emptyPage.setSize(pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getUserId, userIds);
        wrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        wrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        wrapper.orderByAsc(SysUser::getSort);

        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserService.page(page, wrapper);
        
        // 转换为TransferVO分页对象
        List<TransferVO> transferVOList = userPage.getRecords().stream()
                .map(this::convertToTransferVO)
                .collect(Collectors.toList());
        
        IPage<TransferVO> transferVOPage = new Page<>();
        transferVOPage.setRecords(transferVOList);
        transferVOPage.setCurrent(userPage.getCurrent());
        transferVOPage.setSize(userPage.getSize());
        transferVOPage.setTotal(userPage.getTotal());
        
        return transferVOPage;
    }

    @Override
    public IPage<TransferVO> selectUsersInTenantPage(Long tenantId, Integer pageNum, Integer pageSize, String keyword) {
        List<Long> userIds = sysTenantUserService.selectUserIdsByTenantId(tenantId);
        if (CollectionUtils.isEmpty(userIds)) {
            // 返回空分页对象
            IPage<TransferVO> emptyPage = new Page<>();
            emptyPage.setRecords(Collections.emptyList());
            emptyPage.setCurrent(pageNum);
            emptyPage.setSize(pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysUser::getUserId, userIds);
        wrapper.eq(SysUser::getDeleted, DeletedEnum.NO_DELETE.getValue());
        wrapper.eq(SysUser::getStatus, StatusEnum.ENABLE.getValue());
        
        // 添加关键词搜索条件
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword));
        }
        
        wrapper.orderByAsc(SysUser::getSort);

        // 创建分页对象
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        IPage<SysUser> userPage = sysUserService.page(page, wrapper);
        
        // 转换为TransferVO分页对象
        List<TransferVO> transferVOList = userPage.getRecords().stream()
                .map(this::convertToTransferVO)
                .collect(Collectors.toList());
        
        IPage<TransferVO> transferVOPage = new Page<>();
        transferVOPage.setRecords(transferVOList);
        transferVOPage.setCurrent(userPage.getCurrent());
        transferVOPage.setSize(userPage.getSize());
        transferVOPage.setTotal(userPage.getTotal());
        
        return transferVOPage;
    }

    @Override
    public boolean addUserToTenant(Long tenantId, Long userId) {
        // 检查用户是否已存在于租户中
        LambdaQueryWrapper<SysTenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantUser::getTenantId, tenantId)
                 .eq(SysTenantUser::getUserId, userId);
        
        if (sysTenantUserService.count(queryWrapper) > 0) {
            // 用户已存在，无需重复添加
            return true;
        }
        
        // 添加用户到租户
        SysTenantUser tenantUser = new SysTenantUser(userId, tenantId);
        
        return sysTenantUserService.save(tenantUser);
    }

    @Override
    public boolean removeUserFromTenant(Long tenantId, Long userId) {
        // 删除租户用户关联
        LambdaQueryWrapper<SysTenantUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysTenantUser::getTenantId, tenantId)
                 .eq(SysTenantUser::getUserId, userId);
        
        return sysTenantUserService.remove(queryWrapper);
    }
}




