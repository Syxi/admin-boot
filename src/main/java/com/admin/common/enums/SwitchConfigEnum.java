package com.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum SwitchConfigEnum {

    captcha_enabled("captcha_enabled", "验证码开关");


    private String key;

    private String desc;
}
