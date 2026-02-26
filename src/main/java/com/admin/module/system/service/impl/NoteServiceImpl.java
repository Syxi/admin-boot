package com.admin.module.system.service.impl;

import com.admin.module.system.entity.Note;
import com.admin.module.system.mapper.NoteMapper;
import com.admin.module.system.query.NoteQuery;
import com.admin.module.system.service.NoteService;
import com.admin.module.system.vo.NoteVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author sy
* @description 针对表【t_note(笔记表)】的数据库操作Service实现
* @createDate 2024-07-26 14:54:53
*/
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService{

    /**
     * 笔记分页列表
     *
     * @param noteQuery
     * @return
     */
    @Override
    public IPage<NoteVO> selectNotePage(NoteQuery noteQuery) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(noteQuery.getTitle())) {
            wrapper.like(Note::getTitle, noteQuery.getTitle());
        }
        if (StringUtils.isNotBlank(noteQuery.getContent())) {
            wrapper.like(Note::getContent, noteQuery.getContent());
        }
        wrapper.orderByDesc(Note::getCreateTime);
        IPage<Note> page = new Page<>(noteQuery.getPage(), noteQuery.getLimit());
        IPage<Note> notePage = this.page(page, wrapper);

        IPage<NoteVO> noteVOIPage = notePage.convert(note -> {
            NoteVO noteVO = this.convertToNoteVO(note);
            return noteVO;
        });

        return noteVOIPage;
    }

    private NoteVO convertToNoteVO(Note note) {
        NoteVO noteVO = new NoteVO();
        noteVO.setNoteId(note.getNoteId());
        noteVO.setTitle(note.getTitle());
        noteVO.setContent(note.getContent());
        noteVO.setStatus(note.getStatus());
        noteVO.setCreateTime(note.getCreateTime());
        noteVO.setUpdateTime(note.getUpdateTime());
        return noteVO;
    }

    /**
     * 新增笔记
     *
     * @param note
     * @return
     */
    @Override
    public boolean saveNote(Note note) {
        return this.save(note);
    }

    /**
     * 更新笔记
     *
     * @param note
     * @return
     */
    @Override
    public boolean updateNote(Note note) {
        return this.updateById(note);
    }

    /**
     * 删除笔记
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteNote(List<Long> ids) {
        return this.removeByIds(ids);
    }

    /**
     * 获取笔记详情
     *
     * @param noteId
     * @return
     */
    @Override
    public NoteVO getNoteDetail(Long noteId) {
        Note note = this.getById(noteId);
        NoteVO noteVO = this.convertToNoteVO(note);
        return noteVO;
    }
}
