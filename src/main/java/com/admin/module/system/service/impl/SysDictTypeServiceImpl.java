package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.module.system.mapper.SysDictTypeMapper;
import com.admin.module.system.entity.SysDictType;
import com.admin.module.system.query.DictTypeQuery;
import com.admin.module.system.service.SysDictTypeService;
import com.admin.module.system.service.SysDictValueService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典类型
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    private final SysDictValueService dictValueService;


    /**
     * 字典类型分页列表
     * @param dictTypeQuery
     * @return
     */@Override
    public IPage<SysDictType> selectDictTypePage(DictTypeQuery dictTypeQuery) {
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        // 字典类型名称
        if (StringUtils.isNotEmpty(dictTypeQuery.getDictTypeName())) {
            queryWrapper.like(SysDictType::getName, dictTypeQuery.getDictTypeName());
        }
        // 字典类型编码
        if (StringUtils.isNotEmpty(dictTypeQuery.getDictTypeCode())) {
            queryWrapper.like(SysDictType::getTypeCode, dictTypeQuery.getDictTypeCode());
        }
        queryWrapper.orderByAsc(SysDictType::getSort);

        // 分页
        Page<SysDictType> page = new Page<>(dictTypeQuery.getPage(), dictTypeQuery.getLimit());
        Page<SysDictType> dictTypePage = this.page(page, queryWrapper);
        return dictTypePage;
    }



    /**
     * 字典类型详情
     * @param dictTypeId
     * @return
     */
    @Override
    public SysDictType getDictTypeDetail(Long dictTypeId) {
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictType::getId, dictTypeId);
        queryWrapper.last("limit 1");
        SysDictType dictType = this.getOne(queryWrapper);

        Assert.isTrue( dictType != null, "字典类型不存在");
        return dictType;
    }


    /**
     * 新增字典类型
     * @param dictType
     * @return
     */
    @Override
    public boolean saveDictType(SysDictType dictType) {
        return this.save(dictType);
    }



    /**
     * 编辑字典类型
     * @param dictType
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateDictType(SysDictType dictType) {

        SysDictType entity = this.getById(dictType.getId());

       // 更新字典类型
        boolean result = this.updateById(dictType);

        // 字典类型code变化，同步修改字典项的code
        if (result) {
            String oldCode = entity.getTypeCode();
            String newCode = dictType.getTypeCode();
            if (!StringUtils.equals(oldCode, newCode)) {
                dictValueService.updateDictTypeCode(oldCode, newCode);
            }
        }

        return result;
    }


    /**
     * 批量删除
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteDictTypes(List<Long> ids) {

        // 删除字典类型数据
        boolean result = this.removeByIds(ids);

        // 需要删除的roleCoDE
        LambdaQueryWrapper<SysDictType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SysDictType::getId, ids);
        queryWrapper.select(SysDictType::getTypeCode);
        List<SysDictType> dictTypeList = this.list(queryWrapper);
        List<String> dictTypeCodes = dictTypeList.stream()
                .map(SysDictType::getTypeCode)
                .collect(Collectors.toList());

        // 同步删除对应字典项数据
        if (CollectionUtils.isNotEmpty(dictTypeList)) {
            dictValueService.deleteByTypeCode(dictTypeCodes);
        }

        return result;
    }


}
