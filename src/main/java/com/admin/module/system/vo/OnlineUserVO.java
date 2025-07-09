package com.admin.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnlineUserVO {

    private String username;

    private String realName;

    private String ip;

    private LocalDateTime loginTime;
}
