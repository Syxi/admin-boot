package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.common.enums.PublishEnum;
import com.admin.common.enums.TopEnum;
import com.admin.module.system.vo.NoticeVO;
import com.admin.module.system.entity.SysNotice;
import com.admin.module.system.mapper.SysNoticeMapper;
import com.admin.module.system.query.NoticeQuery;
import com.admin.module.system.service.SysNoticeService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_notice(通知公告表)】的数据库操作Service实现
* @createDate 2024-06-03 09:51:41
*/
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice>
    implements SysNoticeService {

    /**
     * 通知分页列表
     *
     * @param noticeQuery
     * @return
     */
    @Override
    public IPage<NoticeVO> selectNoticePage(NoticeQuery noticeQuery) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<SysNotice>();
        if (StringUtils.isNotBlank(noticeQuery.getNoticeTitle())) {
            wrapper.like(SysNotice::getNoticeTitle, noticeQuery.getNoticeTitle());
        }
        wrapper.orderByDesc(SysNotice::getTopTime);
        wrapper.orderByDesc(SysNotice::getCreateTime);
        Page<SysNotice> page = new Page<>(noticeQuery.getPage(), noticeQuery.getLimit());
        IPage<SysNotice> pages = this.page(page, wrapper);
        IPage<NoticeVO> noticeVOIPage = pages.convert(notice -> {
            NoticeVO vo = this.convertToNoticeVO(notice);
            return vo;
        });

        return noticeVOIPage;
    }


    private NoticeVO convertToNoticeVO(SysNotice sysNotice) {
        NoticeVO vo = new NoticeVO();
        vo.setNoticeId(sysNotice.getNoticeId());
        vo.setNoticeTitle(sysNotice.getNoticeTitle());
        vo.setNoticeType(sysNotice.getNoticeType());
        vo.setIsPublish(sysNotice.getIsPublish());
        vo.setIsTop(sysNotice.getIsTop());
        vo.setTopTime(sysNotice.getTopTime());
        vo.setRemark(sysNotice.getRemark());
        vo.setCreateTime(sysNotice.getCreateTime());
        vo.setUpdateTime(sysNotice.getUpdateTime());
        vo.setCreateUser(sysNotice.getCreateUser());
        vo.setUpdateUser(sysNotice.getUpdateUser());

        // html 内容 转换为纯文本
        String noticeContent = sysNotice.getNoticeContent();
        String textContent = (noticeContent == null) ? "" : Jsoup.parse(noticeContent).text();
        vo.setNoticeContent(textContent);
        return vo;
    }

    /**
     * 新增通知
     *
     * @param notice
     * @return
     */
    @Override
    public boolean saveNotice(SysNotice notice) {
        boolean result = this.save(notice);
        return result;
    }

    /**
     * 查看通知
     *
     * @param noticeId
     * @return
     */
    @Override
    public SysNotice getNoticeDetail(Long noticeId) {
        SysNotice notice = this.getById(noticeId);
        return notice;
    }

    /**
     * 更新通知
     *
     * @return
     */
    @Override
    public boolean updateNotice(SysNotice sysNotice) {
        boolean result = this.updateById(sysNotice);
        return result;
    }

    /**
     * 删除通知
     *
     * @param noticeIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteNotice(List<Long> noticeIds) {
        boolean result = this.removeByIds(noticeIds);
        return result;
    }

    /**
     * 置顶通知
     *
     * @param noticeId
     * @return
     */
    @Override
    public boolean topNotice(Long noticeId) {
        LambdaUpdateWrapper<SysNotice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysNotice::getNoticeId, noticeId);
        wrapper.set(SysNotice::getIsTop, TopEnum.IS_TOP.getValue());
        boolean result = this.update(wrapper);
        return result;
    }

    /**
     * 取消置顶通知
     *
     * @param noticeId
     * @return
     */
    @Override
    public boolean cancelTopNotice(Long noticeId) {
        LambdaUpdateWrapper<SysNotice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysNotice::getNoticeId, noticeId);
        wrapper.set(SysNotice::getIsTop, TopEnum.NO_TOP.getValue());
        boolean result = this.update(wrapper);
        return result;
    }

    /**
     * 发布通知
     *
     * @param noticeIds
     * @return
     */
    @Override
    public boolean publishNotices(List<Long> noticeIds) {
        LambdaUpdateWrapper<SysNotice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(SysNotice::getNoticeId, noticeIds);
        wrapper.set(SysNotice::getIsPublish, PublishEnum.IS_PUBLISH.getValue());
        boolean result = this.update(wrapper);
        return result;
    }

    /**
     * 取消发布通知
     *
     * @param noticeIds
     * @return
     */
    @Override
    public boolean cancelPublishNotices(List<Long> noticeIds) {
        LambdaUpdateWrapper<SysNotice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(SysNotice::getNoticeId, noticeIds);
        wrapper.set(SysNotice::getIsPublish, PublishEnum.NO_PUBLISH.getValue());
        boolean result = this.update(wrapper);
        return result;
    }

    /**
     * 门户首页置顶通知
     *
     * @return SysNotice
     */
    @Override
    public SysNotice getTopNotice() {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getIsTop, TopEnum.IS_TOP.getValue());
        wrapper.orderByDesc(SysNotice::getCreateTime);
        wrapper.last("limit 1");
        return this.getOne(wrapper);
    }
}




