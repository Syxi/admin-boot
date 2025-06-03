package com.admin.module.system.vo;

import lombok.Data;

@Data
public class OcrVO {

    // 上传文件名称
    private String name;

    // 上传文件保存路径
    private String fileUrl;

    // 识别文件的文本
    private String text;
}
