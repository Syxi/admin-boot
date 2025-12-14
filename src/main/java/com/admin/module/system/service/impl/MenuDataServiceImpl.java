package com.admin.module.system.service.impl;

import com.admin.common.enums.DeletedEnum;
import com.admin.module.system.entity.SysRole;
import com.admin.module.system.mapper.SysRoleMapper;
import com.admin.module.system.service.MenuDataService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 菜单数据访问服务实现类
 * 专门处理MenuService所需的数据查询操作，避免循环依赖
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuDataServiceImpl implements MenuDataService {
    
    private final SysRoleMapper roleMapper;
    
    /**
     * 根据角色编码获取角色ID集合
     */
    @Override
    public Set<Long> getRoleIdsByRoleCodes(Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Set.of();
        }
        
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysRole::getRoleCode, roleCodes);
        List<SysRole> roles = roleMapper.selectList(queryWrapper);
        
        return roles.stream()
                .map(SysRole::getRoleId)
                .collect(Collectors.toSet());
    }
    
    /**
     * 根据角色ID获取角色
     */
    @Override
    public SysRole getRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }
}