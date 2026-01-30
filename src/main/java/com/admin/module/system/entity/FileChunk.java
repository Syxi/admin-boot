package com.admin.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("file_chunk")
public class FileChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 文件标识符（通常使用文件的MD5）
    private String identifier;

    // 文件名
    private String filename;

    // 文件大小
    private Long totalSize;

    // 总分片数
    private Integer totalChunks;

    // 当前分片索引
    private Integer chunkNumber;

    // 当前分片大小
    private Long chunkSize;

    // 当前分片的MD5
    private String chunkHash;

    // 当前分片的存储路径
    private String chunkPath;

    // 文件是否已上传完成
    private Boolean uploaded;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}