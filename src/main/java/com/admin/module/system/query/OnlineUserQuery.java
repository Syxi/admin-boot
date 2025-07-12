package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OnlineUserQuery extends BasePage {
    private String username;
}
