package com.admin.module.system.dto;


import lombok.Data;

@Data
public class PasswordUpdateDTO {

    private String oldPassword;

    private String newPassword;
}
