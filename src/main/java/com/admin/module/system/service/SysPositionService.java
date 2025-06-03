package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.module.system.entity.SysPosition;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.form.PositionForm;
import com.admin.module.system.query.PositionQuery;
import com.admin.module.system.vo.PositionVO;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_position(岗位表)】的数据库操作Service
* @createDate 2024-05-31 14:06:40
*/
public interface SysPositionService extends IService<SysPosition> {

    /**
     * 岗位分页列表
     * @param positionQuery
     * @return
     */
    IPage<PositionVO> selectSysPositionPage(PositionQuery positionQuery);

    /**
     * 新增岗位
     * @param positionForm
     * @return
     */
    boolean saveSysPosition(PositionForm positionForm);

    /**
     * 获取岗位信息
     * @param id
     * @return
     */
    PositionForm getSysPositionDetail(Long id);

    /**
     * 更新岗位
     * @param positionForm
     * @return
     */
    boolean updateSysPosition(PositionForm positionForm);

    /**
     * 删除岗位
     * @param ids
     * @return
     */
    boolean deleteSysPosition(List<Long> ids);
}
