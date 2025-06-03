package com.admin.module.system.vo;

import lombok.Data;

@Data
public class LoginParams {

    private String username;

    private String password;

    private String captchaKey;

    private String captchaCode;
}
