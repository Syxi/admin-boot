package com.admin.module.system.service;

import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.entity.SysDept;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.form.DeptForm;
import com.admin.module.system.vo.DeptVO;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_dept】的数据库操作Service
* @createDate 2024-05-08 11:16:36
*/
public interface SysDeptService extends IService<SysDept> {


    /**
     * 组织树
     *
     * @return
     */
    List<DeptVO> deptTree(String keyWord);


    /**
     * 组织下拉树
     *
     * @return
     */
    List<OptionVO> deptTreeOptions();


    /**
     * 新增组织
     *
     * @param deptForm
     * @return
     */
    boolean saveDept(DeptForm deptForm);


    /**
     * 获取组织信息
     * @param id
     * @return
     */
    DeptForm getDeptDetail(Long id);

    /**
     * 编辑组织
     *
     * @param deptForm
     * @return
     */
    boolean updateDept(DeptForm deptForm);


    /**
     * 删除组织
     *
     * @param id
     * @return
     */
    boolean deleteDept(Long id);


}
