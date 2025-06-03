package com.admin.module.system.service;

import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.entity.SysOrganization;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.form.OrganizationForm;
import com.admin.module.system.vo.OrganizationVO;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_organization】的数据库操作Service
* @createDate 2024-05-08 11:16:36
*/
public interface SysOrganizationService extends IService<SysOrganization> {


    /**
     * 组织树
     *
     * @return
     */
    List<OrganizationVO> organizationTree(String keyWord);


    /**
     * 组织下拉树
     *
     * @return
     */
    List<OptionVO> organizationTreeOptions();


    /**
     * 新增组织
     *
     * @param organizationForm
     * @return
     */
    boolean saveOrganization(OrganizationForm organizationForm);


    /**
     * 获取组织信息
     * @param id
     * @return
     */
    OrganizationForm getOrganizationDetail(Long id);

    /**
     * 编辑组织
     *
     * @param organizationForm
     * @return
     */
    boolean updateOrganization(OrganizationForm organizationForm);


    /**
     * 删除组织
     *
     * @param id
     * @return
     */
    boolean deleteOrganization(Long id);


}
