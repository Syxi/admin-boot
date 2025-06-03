package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysRoleOrganizationMapper;
import com.admin.module.system.entity.SysRoleOrganization;
import com.admin.module.system.service.SysRoleOrganizationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【sys_role_organization】的数据库操作Service实现
* @createDate 2024-05-16 09:16:55
*/
@Service
public class SysRoleOrganizationServiceImpl extends ServiceImpl<SysRoleOrganizationMapper, SysRoleOrganization>
    implements SysRoleOrganizationService{

    /**
     * 获取组织id
     *
     * @param roleIds
     * @return
     */
    @Override
    public Set<Long> selectOrganIds(List<Long> roleIds) {
        LambdaQueryWrapper<SysRoleOrganization> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysRoleOrganization::getRoleId, roleIds);
        Set<Long> organIds = this.list(wrapper).stream()
                .map(SysRoleOrganization::getOrgId)
                .collect(Collectors.toSet());
        return organIds;
    }
}




