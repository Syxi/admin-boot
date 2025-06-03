package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysDictType;
import com.admin.module.system.query.DictTypeQuery;

import java.util.List;

public interface SysDictTypeService extends IService<SysDictType> {

    /**
     * 字典类型分页列表
     * @param dictTypeQuery
     * @return
     */
    IPage<SysDictType> selectDictTypePage(DictTypeQuery dictTypeQuery);

    /**
     * 字典类型详情
     * @param dictTypeId
     * @return
     */
    SysDictType getDictTypeDetail(Long dictTypeId);

    /**
     * 新增字典类型
     * @param dictType
     * @return
     */
    boolean saveDictType(SysDictType dictType);

    /**
     * 编辑字典类型
     * @param dictType
     * @return
     */
    boolean updateDictType(SysDictType dictType);

    /**
     * 批量删除
     * @param ids
     * @return
     */
    boolean deleteDictTypes(List<Long> ids);





}
