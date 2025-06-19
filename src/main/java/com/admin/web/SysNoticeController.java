package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.SysNotice;
import com.admin.module.system.query.NoticeQuery;
import com.admin.module.system.service.SysNoticeService;
import com.admin.module.system.vo.NoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知公告")
@RequestMapping("/notice")
@RequiredArgsConstructor
@RestController
public class SysNoticeController {

    private final SysNoticeService sysNoticeService;

    @Operation(summary = "通知分页列表")
    @GetMapping("/page")
    public PageResult<NoticeVO> selectNoticePage(NoticeQuery noticeQuery){
        IPage<NoticeVO> noticeIPage = sysNoticeService.selectNoticePage(noticeQuery);
        return PageResult.success(noticeIPage);
    }


    @Operation(summary = "新增通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:add')")
    @NoRepeatSubmit
    @PostMapping("add")
    public ResultVO<Boolean> saveNotice(@RequestBody SysNotice notice){
        boolean result = sysNoticeService.saveNotice(notice);
        return ResultVO.judge(result);
    }


    @Operation(summary = "查看通知")
    @GetMapping("/detail/{noticeId}")
    public ResultVO<SysNotice> getNoticeDetail(@PathVariable("noticeId") Long noticeId){
        SysNotice sysNotice = sysNoticeService.getNoticeDetail(noticeId);
        return ResultVO.success(sysNotice);
    }

    @Operation(summary = "更新通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:edit')")
    @NoRepeatSubmit
    @PutMapping("/update")
    public ResultVO<Boolean> updateNotice(@RequestBody SysNotice notice){
        boolean result = sysNoticeService.updateNotice(notice);
        return ResultVO.judge(result);
    }

    @Operation(summary = "删除通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteNotice(@RequestBody List<Long> noticeIds){
        boolean result = sysNoticeService.deleteNotice(noticeIds);
        return ResultVO.judge(result);
    }


    @Operation(summary = "置顶通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:top')")
    @PutMapping("/top/{noticeId}")
    public ResultVO<Boolean> topNotice(@PathVariable("noticeId") Long noticeId){
        boolean result = sysNoticeService.topNotice(noticeId);
        return ResultVO.judge(result);
    }


    @Operation(summary = "取消置顶通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:cancelTop')")
    @PutMapping("/cancelTop/{noticeId}")
    public ResultVO<Boolean> cancelTopNotice(@PathVariable("noticeId") Long noticeId){
        boolean result = sysNoticeService.cancelTopNotice(noticeId);
        return ResultVO.judge(result);
    }


    @Operation(summary = "发布通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:publish')")
    @PostMapping("/publish")
    public ResultVO<Boolean> publishNotices(@RequestBody List<Long> noticeIds){
        boolean result = sysNoticeService.publishNotices(noticeIds);
        return ResultVO.judge(result);
    }


    @Operation(summary = "取消发布通知")
    @PreAuthorize("@pms.hasPerm('sys:notice:cancelPublish')")
    @PostMapping("/cancelPublish")
    public ResultVO<Boolean> cancelPublishNotices(@RequestBody List<Long> noticeIds){
        boolean result = sysNoticeService.cancelPublishNotices(noticeIds);
        return ResultVO.judge(result);
    }


    @Operation(summary = "门户首页置顶通知")
    @GetMapping("/portal/top")
    public ResultVO<SysNotice> getTopNotice() {
        SysNotice notice = sysNoticeService.getTopNotice();
        return ResultVO.success(notice);
    }

}
