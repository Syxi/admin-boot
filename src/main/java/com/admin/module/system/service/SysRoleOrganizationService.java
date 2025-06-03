package com.admin.module.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysRoleOrganization;

import java.util.List;
import java.util.Set;

/**
* @author sy
* @description 针对表【sys_role_organization】的数据库操作Service
* @createDate 2024-05-16 09:16:55
*/
public interface SysRoleOrganizationService extends IService<SysRoleOrganization> {

    /**
     * 获取组织id
     * @param roleIds
     * @return
     */
    Set<Long> selectOrganIds (List<Long> roleIds);

}
