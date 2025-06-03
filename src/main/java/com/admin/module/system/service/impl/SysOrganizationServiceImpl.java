package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.OrganizationTypeEnum;
import com.admin.common.exception.CustomException;
import com.admin.module.system.entity.SysOrganization;
import com.admin.module.system.form.OrganizationForm;
import com.admin.module.system.mapper.SysOrganizationMapper;
import com.admin.module.system.service.SysOrganizationService;
import com.admin.module.system.service.SysPositionOrganizationService;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.vo.OrganizationVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【sys_organization】的数据库操作Service实现
* @createDate 2024-05-06 10:34:03
*/

@RequiredArgsConstructor
@Service
public class SysOrganizationServiceImpl extends ServiceImpl<SysOrganizationMapper, SysOrganization>
    implements SysOrganizationService {

    private final SysPositionOrganizationService sysPositionOrganizationService;


    /**
     * 组织树
     *
     * @return
     */
    @Override
    public List<OrganizationVO> organizationTree(String searchKeyWord) {
        // 组织列表
        LambdaQueryWrapper<SysOrganization> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(searchKeyWord)) {
            queryWrapper.like(SysOrganization::getOrganName, searchKeyWord);
        }
        queryWrapper.orderByAsc(SysOrganization::getSort);
        List<SysOrganization> organizationList = this.list(queryWrapper);



        // 从根节点，递归添加子列表
        List<OrganizationVO> roots = new ArrayList<>();
        for (SysOrganization org : organizationList) {
            if (org.getParentId() == 0L) {
                roots.add(setChildren(this.convertToOrganVO(org), organizationList));
            }
        }

        return roots;
    }


    /**
     * 递归setChildren
     * children是子机构或部门
     * @param organizationVO
     * @param organizationList
     * @return
     */
    private OrganizationVO setChildren(OrganizationVO organizationVO, List<SysOrganization> organizationList) {
        List<OrganizationVO> children = new ArrayList<>();

        // 递归添加子机构或部门
        for (SysOrganization child : organizationList) {
            if (child.getParentId().equals(organizationVO.getId())) {
                OrganizationVO childVO = this.convertToOrganVO(child);
                // 递归查找子节点
                childVO = setChildren(childVO, organizationList);
                children.add(childVO);
            }
            }

        organizationVO.setChildren(children);
        return organizationVO;
        }


    /**
     * SysOrganization 转换为 OrganizationVO
     * @param sysOrgan
     * @return
     */
    private OrganizationVO convertToOrganVO(SysOrganization sysOrgan) {
        OrganizationVO vo = new OrganizationVO();
        vo.setId(sysOrgan.getId());
        vo.setOrganName(sysOrgan.getOrganName());
        vo.setOrganCode(sysOrgan.getOrganCode());
        vo.setOrganType(sysOrgan.getOrganType());
        vo.setParentId(sysOrgan.getParentId());
        vo.setSort(sysOrgan.getSort());
        vo.setStatus(sysOrgan.getStatus());
        vo.setCreateTime(sysOrgan.getCreateTime());
        vo.setUpdateTime(sysOrgan.getUpdateTime());
        vo.setOrganImg(sysOrgan.getOrganImg());
        vo.setOrganIntroduction(sysOrgan.getOrganIntroduction());
        vo.setOrganPhone(sysOrgan.getOrganPhone());
        vo.setOrganAddress(sysOrgan.getOrganAddress());
        vo.setRemark(sysOrgan.getRemark());
        return vo;
    }






    /**
     * 组织下拉树
     *
     * @return
     */
    @Override
    public List<OptionVO> organizationTreeOptions() {
        List<SysOrganization> organizationList = this.list();

        if (CollectionUtils.isEmpty(organizationList)) {
            return Collections.emptyList();
        }

        List<OrganizationVO> organizationVOS = new ArrayList<>();
        for (SysOrganization organization : organizationList) {
            if (organization.getParentId() == 0L) {
                organizationVOS.add(setChildren(this.convertToOrganVO(organization), organizationList));
            }
        }

        List<OptionVO> optionVOS = organizationVOS.stream()
                .map(org -> this.convertToOption(org))
                .collect(Collectors.toList());
//        for (SysOrganization organization : organizationList) {
//            if (organization.getParentId() == 0L) {
//                OrganizationVO organizationVO = this.convertToOrganVO(organization);
//                organizationVO.setChildren(organization);
//            }
//        }

        return optionVOS;
    }





    /**
     * OrganizationVO 转 Option
     * @param organizationVO
     * @return
     */
    private OptionVO convertToOption(OrganizationVO organizationVO) {
        OptionVO optionVo = new OptionVO();
        optionVo.setValue(organizationVO.getId());
        optionVo.setLabel(organizationVO.getOrganName());

        List<OptionVO> childrenOptionVOS = Optional.ofNullable(organizationVO.getChildren())
                .orElse(Collections.emptyList())
                .stream()
                .map(child -> this.convertToOption(child) )
                .collect(Collectors.toList());

        optionVo.setChildren(childrenOptionVOS);
        return optionVo;
    }



    /**
     * 新增组织
     *
     * @param organizationForm
     * @return
     */
    @Override
    public boolean saveOrganization(OrganizationForm organizationForm) {
        // 校验编码
        boolean checkOrganCodeExist = this.checkOrganCodeExist(organizationForm.getOrganCode(), null);
        if (checkOrganCodeExist) {
            throw new CustomException("编码已存在");
        }

        // 校验机构名称是否重复
        boolean checkOrganNameExist = this.checkOrganNameExist(organizationForm.getOrganName(), null);
        if (checkOrganNameExist) {
            throw new CustomException("机构名称不能重复");
        }

        // 校验同一机构下，部门名称是否重复
        boolean checkDeptNameExist = this.checkDeptNameExist(organizationForm.getOrganName(),
                organizationForm.getParentId(), null);
        if (checkDeptNameExist) {
            throw new CustomException("同一机构下，部门名称不能重复");
        }

        // 新增机构
        SysOrganization sysOrganization = this.convertToSysOrganization(organizationForm);
        return this.save(sysOrganization);

    }


    /**
     * 检验部门编码是否已存在
     * @param organCode
     * @param organId
     * @return
     */
    private boolean checkOrganCodeExist(String organCode, Long organId) {
        LambdaQueryWrapper<SysOrganization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOrganization::getOrganCode, organCode);
        queryWrapper.eq(SysOrganization::getDeleted, DeletedEnum.NO_DELETE.getValue());
        if (organId != null) {
            queryWrapper.ne(SysOrganization::getId, organId);
        }
        return this.exists(queryWrapper);
    }

    /**
     * 校验机构名称是否重复
     * @param organName
     * @param organId
     * @return
     */
    private boolean checkOrganNameExist(String organName, Long organId) {
        LambdaQueryWrapper<SysOrganization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOrganization::getDeleted, DeletedEnum.NO_DELETE.getValue());
        queryWrapper.eq(SysOrganization::getOrganType, OrganizationTypeEnum.ORGANIZATION.getValue());
        queryWrapper.eq(SysOrganization::getOrganName, organName);
        if (organId != null) {
            queryWrapper.ne(SysOrganization::getId, organId);
        }
        return this.exists(queryWrapper);
    }


    /**
     * 检验同一父节点下部门名称是否已存在
     * @param organName
     * @param organParentId
     * @return
     */
    private boolean checkDeptNameExist(String organName, Long organParentId, Long id) {
        LambdaQueryWrapper<SysOrganization> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysOrganization::getParentId, organParentId);
        if (id != null) {
            queryWrapper.ne(SysOrganization::getId, id);
        }

        List<String> organNames = this.list(queryWrapper).stream()
                .map(SysOrganization::getOrganName)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(organNames)) {
            return false;
        }
        return organNames.contains(organName);
    }




    /**
     * OrganizationForm 转换 SysOrganization
     * @param organizationForm
     * @return
     */
    private SysOrganization convertToSysOrganization(OrganizationForm organizationForm) {
        SysOrganization sysOrganization = new SysOrganization();
        sysOrganization.setId(organizationForm.getId());
        sysOrganization.setOrganName(organizationForm.getOrganName());
        sysOrganization.setOrganCode(organizationForm.getOrganCode());
        sysOrganization.setOrganType(organizationForm.getOrganType());
        sysOrganization.setParentId(organizationForm.getParentId());
        sysOrganization.setSort(organizationForm.getSort());
        sysOrganization.setStatus(organizationForm.getStatus());
        sysOrganization.setOrganImg(organizationForm.getOrganImg());
        sysOrganization.setOrganIntroduction(organizationForm.getOrganIntroduction());
        sysOrganization.setOrganPhone(organizationForm.getOrganPhone());
        sysOrganization.setOrganAddress(organizationForm.getOrganAddress());
        sysOrganization.setRemark(organizationForm.getRemark());
        sysOrganization.setCreateTime(organizationForm.getCreateTime());
        sysOrganization.setUpdateTime(organizationForm.getUpdateTime());
        sysOrganization.setCreateUser(organizationForm.getCreateUser());
        sysOrganization.setUpdateUser(organizationForm.getUpdateUser());

        String treePath = this.generateTreePath(organizationForm.getParentId());
        sysOrganization.setTreePath(treePath);

        return sysOrganization;
    }


    /**
     * 获取组织信息
     *
     * @param id
     * @return
     */
    @Override
    public OrganizationForm getOrganizationDetail(Long id) {
        SysOrganization sysOrganization = this.getById(id);

        OrganizationForm organizationForm = new OrganizationForm();
        organizationForm.setId(sysOrganization.getId());
        organizationForm.setOrganName(sysOrganization.getOrganName());
        organizationForm.setOrganCode(sysOrganization.getOrganCode());
        organizationForm.setOrganType(sysOrganization.getOrganType());
        organizationForm.setParentId(sysOrganization.getParentId());
        organizationForm.setTreePath(sysOrganization.getTreePath());
        organizationForm.setSort(sysOrganization.getSort());
        organizationForm.setStatus(sysOrganization.getStatus());
        organizationForm.setCreateTime(sysOrganization.getCreateTime());
        organizationForm.setUpdateTime(sysOrganization.getUpdateTime());
        organizationForm.setCreateUser(sysOrganization.getCreateUser());
        organizationForm.setUpdateUser(sysOrganization.getUpdateUser());
        organizationForm.setOrganImg(sysOrganization.getOrganImg());
        organizationForm.setOrganIntroduction(sysOrganization.getOrganIntroduction());
        organizationForm.setOrganPhone(sysOrganization.getOrganPhone());
        organizationForm.setOrganAddress(sysOrganization.getOrganAddress());
        organizationForm.setRemark(sysOrganization.getRemark());
        return organizationForm;
    }


    /**
     * 编辑组织
     *
     * @param organizationForm
     * @return
     */
    @Override
    public boolean updateOrganization(OrganizationForm organizationForm) {
        // 校验编码
        boolean checkOrganCodeExist = this.checkOrganCodeExist(organizationForm.getOrganCode(), organizationForm.getId());
        if (checkOrganCodeExist) {
            throw new CustomException("编码已存在");
        }

        // 校验机构名称是否重复
        boolean checkOrganNameExist = this.checkOrganNameExist(organizationForm.getOrganName(), organizationForm.getId());
        if (checkOrganNameExist) {
            throw new CustomException("机构名称不能重复");
        }

        // 校验同一机构下，部门名称是否重复
        boolean checkDeptNameExist = this.checkDeptNameExist(organizationForm.getOrganName(),
                organizationForm.getParentId(), organizationForm.getId());
        if (checkDeptNameExist) {
            throw new CustomException("同一机构下，部门名称不能重复");
        }

        SysOrganization sysOrganization = this.convertToSysOrganization(organizationForm);
        return this.updateById(sysOrganization);

    }


    /**
     * 删除组织
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public boolean deleteOrganization(Long id) {
        LambdaUpdateWrapper<SysOrganization> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysOrganization::getId, id)
                .or()
                .apply("CONCAT (',',tree_path,',') LIKE CONCAT('%,',{0},',%')", id);

        List<Long> organIdList = this.list(wrapper).stream()
                .map(SysOrganization::getId)
                .collect(Collectors.toList());

        boolean result =  this.removeByIds(organIdList);
        if (result) {
            sysPositionOrganizationService.deleteSysPositionOrgan(organIdList);
        }

        return result;
    }




    /**
     * 父节点路径
     * @param parentId
     * @return
     */
    private String generateTreePath(Long parentId) {
        if (SystemConstants.ROOT_NODE_ID.equals(parentId)) {
            return String.valueOf(parentId);
        } else {
            SysOrganization sysOrganization = this.getById(parentId);
            return sysOrganization != null ? sysOrganization.getTreePath() + ',' + sysOrganization.getId() : null;
        }
    }




}




