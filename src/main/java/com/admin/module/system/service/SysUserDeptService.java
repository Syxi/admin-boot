package com.admin.module.system.service;

import com.admin.module.system.entity.SysUserDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_user_dept(用户和部门关联表)】的数据库操作Service
* @createDate 2025-06-20 16:28:53
*/
public interface SysUserDeptService extends IService<SysUserDept> {

    /**
     * 根据用户ID, 更新部门ID
     *
     * @param userId
     * @param deptId
     */
    void updateByUserId(Long userId, Long deptId);

    /**
     * 根据用户ID, 删除
     * @param userIds
     */
    void deleteBatchByUserId(List<Long> userIds);

    /**
     * 根据用户ID, 查询部门ID
     * @param userId
     * @return
     */
    Long selectDeptId(Long userId);

}
