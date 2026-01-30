package com.admin.module.system.mapper;

import com.admin.module.system.entity.FileChunk;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileChunkMapper extends BaseMapper<FileChunk> {

    /**
     * 根据文件标识符查询所有已上传的分片
     */
    List<FileChunk> selectChunksByIdentifier(@Param("identifier") String identifier);

    /**
     * 根据文件标识符删除所有分片记录
     */
    int deleteChunksByIdentifier(@Param("identifier") String identifier);
}