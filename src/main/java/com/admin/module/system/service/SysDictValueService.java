package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.entity.SysDictValue;
import com.admin.module.system.query.DictValueQuery;

import java.util.List;

public interface SysDictValueService extends IService<SysDictValue> {

    /**
     * 字典项分页列表
     * @param dictValueQuery
     * @return
     */
    IPage<SysDictValue> selectDictValuePage(DictValueQuery dictValueQuery);


    /**
     * 获取字典详情
     * @param id
     * @return
     */
    SysDictValue getDictValueDetail(Long id);

    /**
     * 添加字典项
     * @param dictValue
     * @return
     */
    boolean selectDictValue(SysDictValue dictValue);

    /**
     * 编辑字典项
     * @param dictValue
     * @return
     */
    boolean updateDictValue(SysDictValue dictValue);

    /**
     * 批量删除字典项
     * @param ids
     * @return
     */
    boolean deleteDictValue(List<Long> ids);


    /**
     * 字典类型code编号， 更新字典项中的 code
     */
    void updateDictTypeCode(String oldCode, String newCode);

    /**
     * 删除字典类型, 同步删除字典类型对应的字典项
     * @param dictTypeCode
     */
    void deleteByTypeCode(List<String> dictTypeCode);

    /**
     * 字典项下拉列表
     * @param typeCode
     * @return
     */
    List<OptionVO> treeOptions(String typeCode);
}
