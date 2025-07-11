package com.admin.module.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OnlineUserVO {

    private String username;

    private Long userId;

    private LocalDateTime loginTime;
}
