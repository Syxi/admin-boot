package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.SysNotice;
import com.admin.module.system.query.NoticeQuery;
import com.admin.module.system.vo.NoticeVO;

import java.util.List;

/**
* @author sy
* @description 针对表【sys_notice(通知公告表)】的数据库操作Service
* @createDate 2024-06-03 09:51:41
*/
public interface SysNoticeService extends IService<SysNotice> {

    /**
     * 通知分页列表
     * @param noticeQuery
     * @return
     */
    IPage<NoticeVO> selectNoticePage(NoticeQuery noticeQuery);

    /**
     * 新增通知
     * @param notice
     * @return
     */
    boolean saveNotice(SysNotice notice);

    /**
     * 查看通知
     * @param noticeId
     * @return
     */
    SysNotice getNoticeDetail(Long noticeId);

    /**
     * 更新通知
     * @return
     */
    boolean updateNotice(SysNotice notice);

    /**
     * 删除通知
     * @param noticeIds
     * @return
     */
    boolean deleteNotice(List<Long> noticeIds);

    /**
     * 置顶通知
     * @param noticeId
     * @return
     */
    boolean topNotice(Long noticeId);


    /**
     * 取消置顶通知
     * @param noticeId
     * @return
     */
    boolean cancelTopNotice(Long noticeId);

    /**
     * 发布通知
     * @param noticeIds
     * @return
     */
    boolean publishNotices(List<Long> noticeIds);


    /**
     * 取消发布通知
     * @param noticeIds
     * @return
     */
    boolean cancelPublishNotices(List<Long> noticeIds);

    /**
     * 门户首页置顶通知
     * @return SysNotice
     */
    SysNotice getTopNotice();
}
