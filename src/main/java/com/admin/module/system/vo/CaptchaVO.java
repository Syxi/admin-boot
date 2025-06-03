package com.admin.module.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 验证码响应对象
 * @Author: suYan
 * @Date: 2023-08-07
 */
@Schema(description = "验证码响应对象")
@Builder
@Data
public class CaptchaVO {

    @Schema(description = "验证码缓存key")
    private String captchaKey;

    @Schema(description = "验证码图片Base64字符串")
    private String captchaBase64;
}
