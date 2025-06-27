package com.admin.module.system.service.impl;

import com.admin.common.constant.SystemConstants;
import com.admin.common.enums.DeletedEnum;
import com.admin.common.enums.OrganizationTypeEnum;
import com.admin.common.exception.CustomException;
import com.admin.module.system.entity.SysDept;
import com.admin.module.system.form.DeptForm;
import com.admin.module.system.mapper.SysDeptMapper;
import com.admin.module.system.service.SysDeptService;
import com.admin.module.system.service.SysPositionDeptService;
import com.admin.module.system.vo.DeptVO;
import com.admin.module.system.vo.OptionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【sys_dept】的数据库操作Service实现
* @createDate 2024-05-06 10:34:03
*/

@RequiredArgsConstructor
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept>
    implements SysDeptService {

    private final SysPositionDeptService sysPositionDeptService;


    /**
     * 组织树
     *
     * @return
     */
    @Override
    public List<DeptVO> deptTree(String keyWord) {
        // 组织列表
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
//        if (StringUtils.isNotBlank(keyWord)) {
//            queryWrapper.like(SysDept::getDeptName, keyWord);
//        }
        queryWrapper.orderByAsc(SysDept::getSort);
        List<SysDept> deptList = this.list(queryWrapper);

        // 如果没有结果，直接返回空列表
        if (CollectionUtils.isEmpty(deptList)) {
            return Collections.emptyList();
        }

        // 如果没有关键词，直接构建整个树
        if (StringUtils.isBlank(keyWord)) {
            return buildDeptTree(deptList);
        }

        // 筛选出名称匹配的部门
        List<SysDept> matchedDepts = deptList.stream()
                .filter(dept -> dept.getDeptName().contains(keyWord))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(matchedDepts)) {
            return Collections.emptyList();
        }

        // 收集所有匹配节点及其祖先节点 ID
        Set<Long> idsToShow = new HashSet<>();
        for (SysDept dept : matchedDepts) {
            Long currentId = dept.getId();
            idsToShow.add(currentId);

            String treePath = dept.getTreePath();
            if (StringUtils.isNotBlank(treePath)) {
                Arrays.stream(treePath.split(","))
                        .map(Long::valueOf)
                        .forEach(idsToShow::add);
            }
        }

        // 过滤出需要展示的部门列表
        List<SysDept> filteredDeptList = deptList.stream()
                .filter(dept -> idsToShow.contains(dept.getId()))
                .collect(Collectors.toList());

        return buildDeptTree(filteredDeptList);
    }


    private List<DeptVO> buildDeptTree(List<SysDept> deptList) {
        // 构建 parentId -> 子部门映射
        Map<Long, List<SysDept>> childrenMap = new HashMap<>();
        for (SysDept dept : deptList) {
            childrenMap.computeIfAbsent(dept.getParentId(), k -> new ArrayList<>()).add(dept);
        }

        List<DeptVO> roots = new ArrayList<>();
        for (SysDept dept : deptList) {
            if (dept.getParentId() == 0L) {
                roots.add(setChildrenWithMap(convertToDeptVO(dept), childrenMap));
            }
        }

        return roots;
    }

    private DeptVO setChildrenWithMap(DeptVO deptVO, Map<Long, List<SysDept>> childrenMap) {
        List<DeptVO> children = new ArrayList<>();

        List<SysDept> directChildren = childrenMap.getOrDefault(deptVO.getId(), Collections.emptyList());
        for (SysDept child : directChildren) {
            DeptVO childVO = convertToDeptVO(child);
            childVO = setChildrenWithMap(childVO, childrenMap); // 递归构建子树
            children.add(childVO);
        }

        deptVO.setChildren(children);
        return deptVO;
    }




    /**
     * SysDept 转换为 DeptVO
     * @param sysdept
     * @return
     */
    private DeptVO convertToDeptVO(SysDept sysdept) {
        DeptVO vo = new DeptVO();
        vo.setId(sysdept.getId());
        vo.setDeptName(sysdept.getDeptName());
        vo.setDeptCode(sysdept.getDeptCode());
        vo.setDeptType(sysdept.getDeptType());
        vo.setParentId(sysdept.getParentId());
        vo.setSort(sysdept.getSort());
        vo.setStatus(sysdept.getStatus());
        vo.setCreateTime(sysdept.getCreateTime());
        vo.setUpdateTime(sysdept.getUpdateTime());
        vo.setDeptImg(sysdept.getDeptImg());
        vo.setDeptIntroduction(sysdept.getDeptIntroduction());
        vo.setDeptPhone(sysdept.getDeptPhone());
        vo.setDeptAddress(sysdept.getDeptAddress());
        vo.setRemark(sysdept.getRemark());
        return vo;
    }






    /**
     * 组织下拉树
     *
     * @return
     */
    @Override
    public List<OptionVO> deptTreeOptions() {
        List<SysDept> deptList = this.list();

        if (CollectionUtils.isEmpty(deptList)) {
            return Collections.emptyList();
        }

        List<DeptVO> deptVOS = new ArrayList<>();
        for (SysDept sysDept : deptList) {
            if (sysDept.getParentId() == 0L) {
                deptVOS.add(setChildren(this.convertToDeptVO(sysDept), deptList));
            }
        }

        List<OptionVO> optionVOS = deptVOS.stream()
                .map(dept -> this.convertToOption(dept))
                .collect(Collectors.toList());

        return optionVOS;
    }


    /**
     * 递归setChildren
     * children是子机构或部门
     * @param deptVO
     * @param deptList
     * @return
     */
    private DeptVO setChildren(DeptVO deptVO, List<SysDept> deptList) {
        List<DeptVO> children = new ArrayList<>();

        // 递归添加子机构或部门
        for (SysDept child : deptList) {
            if (child.getParentId().equals(deptVO.getId())) {
                DeptVO childVO = this.convertToDeptVO(child);
                // 递归查找子节点
                childVO = setChildren(childVO, deptList);
                children.add(childVO);
            }
        }

        deptVO.setChildren(children);
        return deptVO;
    }



    /**
     * DeptVO 转 Option
     * @param deptVO
     * @return
     */
    private OptionVO convertToOption(DeptVO deptVO) {
        OptionVO optionVo = new OptionVO();
        optionVo.setValue(deptVO.getId());
        optionVo.setLabel(deptVO.getDeptName());

        List<OptionVO> childrenOptionVOS = Optional.ofNullable(deptVO.getChildren())
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
     * @param deptForm
     * @return
     */
    @Override
    public boolean saveDept(DeptForm deptForm) {
        // 校验编码
        boolean checkDeptCodeExist = this.checkOrganCodeExist(deptForm.getDeptCode(), null);
        if (checkDeptCodeExist) {
            throw new CustomException("编码已存在");
        }

        // 校验机构名称是否重复
        boolean checkOrganNameExist = this.checkOrganNameExist(deptForm.getDeptName(), null);
        if (checkOrganNameExist) {
            throw new CustomException("机构名称不能重复");
        }

        // 校验同一机构下，部门名称是否重复
        boolean checkDeptNameExist = this.checkDeptNameExist(deptForm.getDeptName(),
                deptForm.getParentId(), null);
        if (checkDeptNameExist) {
            throw new CustomException("同一机构下，部门名称不能重复");
        }

        // 新增机构
        SysDept sysDept = this.convertToDept(deptForm);
        return this.save(sysDept);

    }


    /**
     * 检验部门编码是否已存在
     * @param organCode
     * @param organId
     * @return
     */
    private boolean checkOrganCodeExist(String organCode, Long organId) {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getDeptCode, organCode);
        queryWrapper.eq(SysDept::getDeleted, DeletedEnum.NO_DELETE.getValue());
        if (organId != null) {
            queryWrapper.ne(SysDept::getId, organId);
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
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getDeleted, DeletedEnum.NO_DELETE.getValue());
        queryWrapper.eq(SysDept::getDeptType, OrganizationTypeEnum.ORGANIZATION.getValue());
        queryWrapper.eq(SysDept::getDeptName, organName);
        if (organId != null) {
            queryWrapper.ne(SysDept::getId, organId);
        }
        return this.exists(queryWrapper);
    }


    /**
     * 检验同一父节点下部门名称是否已存在
     * @param deptName
     * @param organParentId
     * @return
     */
    private boolean checkDeptNameExist(String deptName, Long organParentId, Long id) {
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getParentId, organParentId);
        if (id != null) {
            queryWrapper.ne(SysDept::getId, id);
        }

        List<String> deptNames = this.list(queryWrapper).stream()
                .map(SysDept::getDeptName)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deptNames)) {
            return false;
        }
        return deptNames.contains(deptName);
    }




    /**
     * DeptForm 转换 SysDept
     * @param deptForm
     * @return
     */
    private SysDept convertToDept(DeptForm deptForm) {
        SysDept sysDept = new SysDept();
        sysDept.setId(deptForm.getId());
        sysDept.setDeptName(deptForm.getDeptName());
        sysDept.setDeptCode(deptForm.getDeptCode());
        sysDept.setDeptType(deptForm.getDeptType());
        sysDept.setParentId(deptForm.getParentId());
        sysDept.setSort(deptForm.getSort());
        sysDept.setStatus(deptForm.getStatus());
        sysDept.setDeptImg(deptForm.getDeptImg());
        sysDept.setDeptIntroduction(deptForm.getDeptIntroduction());
        sysDept.setDeptPhone(deptForm.getDeptPhone());
        sysDept.setDeptAddress(deptForm.getDeptAddress());
        sysDept.setRemark(deptForm.getRemark());
        sysDept.setCreateTime(deptForm.getCreateTime());
        sysDept.setUpdateTime(deptForm.getUpdateTime());
        sysDept.setCreateUser(deptForm.getCreateUser());
        sysDept.setUpdateUser(deptForm.getUpdateUser());

        String treePath = this.generateTreePath(deptForm.getParentId());
        sysDept.setTreePath(treePath);

        return sysDept;
    }


    /**
     * 获取组织信息
     *
     * @param id
     * @return
     */
    @Override
    public DeptForm getDeptDetail(Long id) {
        SysDept sysDept = this.getById(id);

        DeptForm deptForm = new DeptForm();
        deptForm.setId(sysDept.getId());
        deptForm.setDeptName(sysDept.getDeptName());
        deptForm.setDeptCode(sysDept.getDeptCode());
        deptForm.setDeptType(sysDept.getDeptType());
        deptForm.setParentId(sysDept.getParentId());
        deptForm.setTreePath(sysDept.getTreePath());
        deptForm.setSort(sysDept.getSort());
        deptForm.setStatus(sysDept.getStatus());
        deptForm.setCreateTime(sysDept.getCreateTime());
        deptForm.setUpdateTime(sysDept.getUpdateTime());
        deptForm.setCreateUser(sysDept.getCreateUser());
        deptForm.setUpdateUser(sysDept.getUpdateUser());
        deptForm.setDeptImg(sysDept.getDeptImg());
        deptForm.setDeptIntroduction(sysDept.getDeptIntroduction());
        deptForm.setDeptPhone(sysDept.getDeptPhone());
        deptForm.setDeptAddress(sysDept.getDeptAddress());
        deptForm.setRemark(sysDept.getRemark());
        return deptForm;
    }


    /**
     * 编辑组织
     *
     * @param deptForm
     * @return
     */
    @Override
    public boolean updateDept(DeptForm deptForm) {
        // 校验编码
        boolean checkOrganCodeExist = this.checkOrganCodeExist(deptForm.getDeptCode(), deptForm.getId());
        if (checkOrganCodeExist) {
            throw new CustomException("编码已存在");
        }

        // 校验机构名称是否重复
        boolean checkOrganNameExist = this.checkOrganNameExist(deptForm.getDeptName(), deptForm.getId());
        if (checkOrganNameExist) {
            throw new CustomException("机构名称不能重复");
        }

        // 校验同一机构下，部门名称是否重复
        boolean checkDeptNameExist = this.checkDeptNameExist(deptForm.getDeptName(),
                deptForm.getParentId(), deptForm.getId());
        if (checkDeptNameExist) {
            throw new CustomException("同一机构下，部门名称不能重复");
        }

        SysDept sysDept = this.convertToDept(deptForm);
        return this.updateById(sysDept);

    }


    /**
     * 删除组织
     *
     * @param id
     * @return
     */
    @Transactional
    @Override
    public boolean deleteDept(Long id) {
        LambdaUpdateWrapper<SysDept> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysDept::getId, id)
                .or()
                .apply("CONCAT (',',tree_path,',') LIKE CONCAT('%,',{0},',%')", id);

        List<Long> deptIdList = this.list(wrapper).stream()
                .map(SysDept::getId)
                .collect(Collectors.toList());

        boolean result =  this.removeByIds(deptIdList);
        if (result) {
            sysPositionDeptService.deleteSysPositionDeptList(deptIdList);
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
            SysDept sysDept = this.getById(parentId);
            return sysDept != null ? sysDept.getTreePath() + ',' + sysDept.getId() : null;
        }
    }




}




