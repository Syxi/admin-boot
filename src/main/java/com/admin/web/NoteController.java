package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.Note;
import com.admin.module.system.query.NoteQuery;
import com.admin.module.system.service.NoteService;
import com.admin.module.system.vo.NoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "笔记管理")
@RequestMapping("/api/note")
@RequiredArgsConstructor
@RestController
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "笔记分页列表")
    @GetMapping("/page")
    public PageResult<NoteVO> selectNotePage(NoteQuery noteQuery) {
        IPage<NoteVO> noteIPage = noteService.selectNotePage(noteQuery);
        return PageResult.success(noteIPage);
    }

    @Operation(summary = "新增笔记")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('note:add')")
    @PostMapping("/add")
    public ResultVO<Boolean> saveNote(@RequestBody Note note) {
        boolean result = noteService.saveNote(note);
        return ResultVO.success(result);
    }

    @Operation(summary = "获取笔记详情")
    @GetMapping("/detail/{id}")
    public ResultVO<NoteVO> getNoteDetail(@PathVariable("id") Long id) {
        NoteVO noteVO = noteService.getNoteDetail(id);
        return ResultVO.success(noteVO);
    }

    @Operation(summary = "修改笔记")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('note:edit')")
    @PutMapping("/edit")
    public ResultVO<Boolean> updateNote(@RequestBody Note note) {
        boolean result = noteService.updateNote(note);
        return ResultVO.success(result);
    }

    @Operation(summary = "删除笔记")
    @PreAuthorize("@pms.hasPerm('note:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteNote(@RequestBody List<Long> ids) {
        boolean result = noteService.deleteNote(ids);
        return ResultVO.success(result);
    }
}
