package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.Note;
import com.admin.module.system.query.NoteQuery;
import com.admin.module.system.vo.NoteVO;

import java.util.List;

/**
* @author sy
* @description 针对表【t_note(笔记表)】的数据库操作Service
* @createDate 2024-07-26 14:54:53
*/
public interface NoteService extends IService<Note> {

    /**
     * 笔记分页列表
     * @param noteQuery
     * @return
     */
    IPage<NoteVO> selectNotePage(NoteQuery noteQuery);

    /**
     * 新增笔记
     * @param note
     * @return
     */
    boolean saveNote(Note note);

    /**
     * 更新笔记
     * @param note
     * @return
     */
    boolean updateNote(Note note);

    /**
     * 删除笔记
     * @param ids
     * @return
     */
    boolean deleteNote(List<Long> ids);

    /**
     * 获取笔记详情
     * @param noteId
     * @return
     */
    NoteVO getNoteDetail(Long noteId);
}
