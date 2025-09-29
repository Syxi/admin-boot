package com.admin.module.system.service.impl;

import com.admin.common.enums.StatusEnum;
import com.admin.common.exception.CustomException;
import com.admin.module.system.entity.Platform;
import com.admin.module.system.mapper.PlatformMapper;
import com.admin.module.system.service.PlatformService;
import com.admin.module.system.vo.PlatformVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 苏彦
* @description 针对表【t_platform(平台系统表)】的数据库操作Service实现
* @createDate 2025-09-25 15:33:49
*/
@Service
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, Platform>
    implements PlatformService{

    /**
     * @return
     */
    @Override
    public IPage<PlatformVO> selectPage(PlatformVO platformVO) {
        LambdaQueryWrapper<Platform> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(platformVO.getName())) {
            wrapper.like(Platform::getName, platformVO.getName());
        }
        wrapper.orderByAsc(Platform::getSort);
        Page<Platform> page = new Page<>(platformVO.getPage(), platformVO.getLimit());
        Page<Platform> platformPage = this.page(page, wrapper);
        return platformPage.convert(platform -> {
            PlatformVO formVO = new PlatformVO();
            formVO.setId(platform.getId());
            formVO.setName(platform.getName());
            formVO.setPath(platform.getPath());
            formVO.setSort(platform.getSort());
            formVO.setStatus(platform.getStatus());
            formVO.setIcon(platform.getIcon());
            formVO.setRemake(platform.getRemake());
            formVO.setCreateTime(platform.getCreateTime());
            formVO.setUpdateTime(platform.getUpdateTime());
            return formVO;
        });
    }

    /**
     * 新增平台系统
     * @param platform
     * @return
     */
    @Override
    public boolean savePlatform(Platform platform) {
        this.checkPlatformExist(platform.getName(), null);
        return this.save(platform);
    }

    /**
     * 校验平台系统名称是否已存在
     * @param platformName
     * @return
     */
    private void checkPlatformExist(String platformName, Long id) {
        LambdaQueryWrapper<Platform> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Platform::getName, platformName);
        if (id != null) {
            wrapper.ne(Platform::getId, id);
        }
        boolean result = this.baseMapper.exists(wrapper);
        if (result) {
            throw new CustomException("平台系统名称已存在");
        }
    }

    /**
     * 更新平台系统
     * @param platform
     * @return
     */
    @Override
    public boolean updatePlatform(Platform platform) {
        this.checkPlatformExist(platform.getName(), platform.getId());
        return this.updateById(platform);
    }

    /**
     * 查询平台系统首页列表
     *
     * @return
     */
    @Override
    public List<Platform> selectPlatformList() {
        LambdaQueryWrapper<Platform> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Platform::getStatus, StatusEnum.ENABLE.getValue());
        wrapper.orderByAsc(Platform::getSort);
        return this.list(wrapper);
    }
}




