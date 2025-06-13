package com.admin.module.system.service.impl;

import com.admin.module.system.mapper.SysPositionMapper;
import com.admin.module.system.service.SysDeptService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.entity.SysDept;
import com.admin.module.system.entity.SysPosition;
import com.admin.module.system.entity.SysPositionDept;
import com.admin.module.system.form.PositionForm;
import com.admin.module.system.query.PositionQuery;
import com.admin.module.system.vo.PositionVO;
import com.admin.module.system.service.SysPositionDeptService;
import com.admin.module.system.service.SysPositionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【sys_position(岗位表)】的数据库操作Service实现
* @createDate 2024-05-31 14:06:40
*/
@Service
@RequiredArgsConstructor
public class SysPositionServiceImpl extends ServiceImpl<SysPositionMapper, SysPosition>
    implements SysPositionService{

    private final SysPositionDeptService positionOrganService;

    private final SysDeptService sysDeptService;

    /**
     * 岗位分页列表
     *
     * @param positionQuery
     * @return
     */
    @Override
    public IPage<PositionVO> selectSysPositionPage(PositionQuery positionQuery) {
        LambdaQueryWrapper<SysPosition> wrapper = new LambdaQueryWrapper<SysPosition>();
        if (StringUtils.isNotBlank(positionQuery.getPositionName())) {
            wrapper.like(SysPosition::getPositionName, positionQuery.getPositionName());
        }
        wrapper.orderByAsc(SysPosition::getSort);
        IPage<SysPosition> page = new Page<>(positionQuery.getPage(), positionQuery.getLimit());
        IPage<SysPosition> sysPositionIPage = this.page(page, wrapper);
        IPage<PositionVO> positionVOIPage = sysPositionIPage.convert(sysPosition -> {
            PositionVO positionVO = this.convertPositionVO(sysPosition);
            String organName = this.getPositionIdOrganNameMap().get(sysPosition.getPositionId());
            positionVO.setOrganName(organName);
            return positionVO;
        });

        return positionVOIPage;
    }


    /**
     * SysPosition 转换成 PositionVO
     * @param sysPosition
     * @return
     */
    private PositionVO convertPositionVO(SysPosition sysPosition) {
        PositionVO positionVO = new PositionVO();
        positionVO.setPositionId(sysPosition.getPositionId());
        positionVO.setPositionName(sysPosition.getPositionName());
        positionVO.setDescription(sysPosition.getDescription());
        positionVO.setStatus(sysPosition.getStatus());
        positionVO.setSort(sysPosition.getSort());
        positionVO.setSalaryRange(sysPosition.getSalaryRange());
        positionVO.setExperience(sysPosition.getExperience());
        positionVO.setEducation(sysPosition.getEducation());
        positionVO.setCreateTime(sysPosition.getCreateTime());
        positionVO.setUpdateTime(sysPosition.getUpdateTime());
        return positionVO;
    }

    /**
     * 构建 positionId 和 organName 的映射
     * @return
     */
    private Map<Long, String> getPositionIdOrganNameMap() {
        Map<Long, Long> positionIdOrganIdMap = positionOrganService.list().stream()
                .collect(Collectors.toMap(SysPositionDept::getPositionId, SysPositionDept::getDeptId));

        Map<Long, String> organIdOrganNameMap = sysDeptService.list().stream()
                .collect(Collectors.toMap(SysDept::getId, SysDept::getDeptName));

        // 构建一个新 Map
        Map<Long, String> positionIdOrganNameMap = new HashMap<>();
        for(Map.Entry<Long, Long> entry : positionIdOrganIdMap.entrySet()) {
            Long positionId = entry.getKey();
            Long organId = entry.getValue();

            String organName = organIdOrganNameMap.get(organId);

            if(StringUtils.isNotBlank(organName)) {
                positionIdOrganNameMap.put(positionId, organName);
            }
        }

        return positionIdOrganNameMap;
    }




    /**
     * 新增岗位
     *
     * @param positionForm
     * @return
     */
    @Override
    public boolean saveSysPosition(PositionForm positionForm) {
        SysPosition sysPosition = this.convertSysPosition(positionForm);
        boolean result = this.save(sysPosition);
        if (result && positionForm.getOrganId() != null) {
            positionOrganService.saveSysPositionDept(sysPosition.getPositionId(), positionForm.getOrganId());
        }
        return result;
    }

    /**
     * PositionForm 转换成 SysPosition
     * @param positionForm
     * @return
     */
    private SysPosition convertSysPosition(PositionForm positionForm) {
        SysPosition sysPosition = new SysPosition();
        sysPosition.setPositionId(positionForm.getPositionId());
        sysPosition.setPositionName(positionForm.getPositionName());
        sysPosition.setDescription(positionForm.getDescription());
        sysPosition.setStatus(positionForm.getStatus());
        sysPosition.setSort(positionForm.getSort());
        sysPosition.setSalaryRange(positionForm.getSalaryRange());
        sysPosition.setExperience(positionForm.getExperience());
        sysPosition.setEducation(positionForm.getEducation());
        return sysPosition;
    }




    /**
     * 获取岗位信息
     *
     * @param id
     * @return
     */
    @Override
    public PositionForm getSysPositionDetail(Long id) {
        SysPosition sysPosition = this.getById(id);

        Long organId = positionOrganService.getDeptId(id);
        PositionForm  positionForm = new PositionForm();
        positionForm.setPositionId(sysPosition.getPositionId());
        positionForm.setPositionName(sysPosition.getPositionName());
        positionForm.setOrganId(organId);
        positionForm.setDescription(sysPosition.getDescription());
        positionForm.setStatus(sysPosition.getStatus());
        positionForm.setSort(sysPosition.getSort());
        positionForm.setSalaryRange(sysPosition.getSalaryRange());
        positionForm.setExperience(sysPosition.getExperience());
        positionForm.setEducation(sysPosition.getEducation());
        return positionForm;
    }

    /**
     * 更新岗位
     *
     * @param positionForm
     * @return
     */
    @Override
    public boolean updateSysPosition(PositionForm positionForm) {
        SysPosition sysPosition = this.convertSysPosition(positionForm);
        boolean result = this.updateById(sysPosition);
        if (result) {
            positionOrganService.updateSysPositionDept(positionForm.getPositionId(), positionForm.getOrganId());
        }
        return result;
    }

    /**
     * 删除岗位
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteSysPosition(List<Long> ids) {
        boolean result = this.removeByIds(ids);
        if (result) {
            positionOrganService.deleteSysPositionDept(ids);
        }
        return result;
    }
}




