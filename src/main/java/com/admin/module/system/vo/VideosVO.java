package com.admin.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideosVO {
    private Long id;

    /**
     * 视频名称
     */
    private String fileName;

    /**
     * 视频的描述
     */
    private String description;

    /**
     * 视频文件的存储路径
     */
    private String filePath;

    /**
     * 请求访问视频 url
     */
    private String url;

    /**
     * 备注
     */
    private String remark;


    /**
     * 文件上传时间
     */
    private LocalDateTime createTime;


    /**
     * 上传者id
     */
    private Long createUser;


}
