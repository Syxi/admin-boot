package com.admin.module.system.service.impl;

import com.admin.module.system.entity.SysUser;
import com.admin.module.system.service.UserTokenRefreshService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Token刷新服务实现类
 * 专门处理用户Token刷新相关逻辑，避免循环依赖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTokenRefreshServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserTokenRefreshService {
    
    /**
     * 根据用户ID列表获取用户名
     *
     * @param userIds 用户ID列表
     * @return 用户名列表
     */
    @Override
    public List<String> getUsernamesByIds(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysUser::getUserId, userIds);
        queryWrapper.select(SysUser::getUsername); // 只查询username字段

        List<SysUser> userList = this.list(queryWrapper);
        return userList.stream()
                .map(SysUser::getUsername)
                .collect(Collectors.toList());
    }
}