package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysDictValueMapper;
import com.admin.module.system.vo.OptionVO;
import com.admin.module.system.entity.SysDictValue;
import com.admin.module.system.query.DictValueQuery;
import com.admin.module.system.service.SysDictValueService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典项
 */
@Service
public class SysDictValueServiceImpl extends ServiceImpl<SysDictValueMapper, SysDictValue> implements SysDictValueService {

    /**
     * 字典项分页列表
     * @param dictValueQuery
     * @return
     */
    @Override
    public IPage<SysDictValue> selectDictValuePage(DictValueQuery dictValueQuery) {
        LambdaQueryWrapper<SysDictValue> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(dictValueQuery.getName())) {
            queryWrapper.like(SysDictValue::getName, dictValueQuery.getName());
        }
        if (StringUtils.isNotEmpty(dictValueQuery.getTypeCode())) {
            queryWrapper.eq(SysDictValue::getTypeCode, dictValueQuery.getTypeCode());
        }
        queryWrapper.orderByAsc(SysDictValue::getSort);

        IPage<SysDictValue> page = new Page<>(dictValueQuery.getPage(), dictValueQuery.getLimit());
        IPage<SysDictValue> dictValuePage = this.page(page, queryWrapper);

        return dictValuePage;
    }



    /**
     * 获取字典项详情
     * @param id
     * @return
     */
    @Override
    public SysDictValue getDictValueDetail(Long id) {
        SysDictValue dictValue = this.getById(id);
        return dictValue;
    }


    /**
     * 添加字典项
     * @param dictValue
     * @return
     */
    @Override
    public boolean selectDictValue(SysDictValue dictValue) {
        return this.save(dictValue);
    }



    /**
     * 编辑字典项
     * @param dictValue
     * @return
     */
    @Override
    public boolean updateDictValue(SysDictValue dictValue) {
        return this.updateById(dictValue);
    }


    /**
     * 批量删除字典项
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDictValue(List<Long> ids) {
        return this.removeBatchByIds(ids);
    }

    /**
     * 字典类型code编号， 更新字典项中的 code
     *
     * @param oldCode
     * @param newCode
     */
    @Override
    public void updateDictTypeCode(String oldCode, String newCode) {
        LambdaUpdateWrapper<SysDictValue> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysDictValue::getTypeCode, oldCode);
        wrapper.set(SysDictValue::getTypeCode, newCode);
        this.update(wrapper);
    }

    /**
     * 删除字典类型, 同步删除字典类型对应的字典项
     *
     * @param dictTypeCode
     */
    @Override
    public void deleteByTypeCode(List<String> dictTypeCode) {
        LambdaQueryWrapper<SysDictValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysDictValue::getTypeCode, dictTypeCode);
        long result = this.count(wrapper);

        if (result > 0) {
            this.remove(wrapper);
        }
    }

    /**
     * 字典项下拉列表
     *
     * @param typeCode
     * @return
     */
    @Override
    public List<OptionVO> treeOptions(String typeCode) {
        LambdaQueryWrapper<SysDictValue> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictValue::getTypeCode, typeCode);
        wrapper.orderByAsc(SysDictValue::getValue);
        wrapper.select(SysDictValue::getValue, SysDictValue::getName);
        List<SysDictValue> dictValueList = this.list(wrapper);

        // 转换
        List<OptionVO> optionVOList = dictValueList.stream()
                .map(dictValue -> new OptionVO(dictValue.getValue(), dictValue.getName()))
                .collect(Collectors.toList());
        return optionVOList;
    }


}
