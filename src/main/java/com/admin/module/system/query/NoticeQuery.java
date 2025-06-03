package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NoticeQuery extends BasePage {

    private String  noticeTitle;
}
