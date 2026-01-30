package com.admin.module.system.service;

import com.admin.module.system.entity.FileChunk;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileChunkService extends IService<FileChunk> {

    /**
     * 检查分片是否已存在
     */
    boolean checkChunkExists(String identifier, Integer chunkNumber);

    /**
     * 上传分片
     */
    boolean uploadChunk(String identifier, Integer chunkNumber, Long totalChunks, Long totalSize, String filename, String chunkHash, MultipartFile file);

    /**
     * 合并分片
     */
    boolean mergeChunks(String identifier, String filename, String fileMd5, String fileType, String description);

    /**
     * 查询已上传的分片列表
     */
    List<Integer> getUploadedChunks(String identifier);

    /**
     * 删除临时分片文件和记录
     */
    boolean deleteTempChunks(String identifier);
}