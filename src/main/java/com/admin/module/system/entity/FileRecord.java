package com.admin.module.system.entity;

import com.admin.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileRecord extends BaseEntity {

    // 文件id
    private Long id;

    // 文件名称
    private String fileName;

    // MD5，文件的唯一标识
    private String fileMd5;

    // 文件大小
    private String fileSize;

    // 文件类型
    private String fileType;

    // 文件描述/标签
    private String fileDescription;

    // 文件存储路径
    private String fileStoragePath;

    // 转换成pdf文件的存储路径
    private String pdfStoragePath;

    // 文件转换状态
    private Integer fileConversionStatus;
}
