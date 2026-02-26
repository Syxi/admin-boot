package com.admin.module.system.query;

import com.admin.common.base.BasePage;
import lombok.Data;

@Data
public class NoteQuery extends BasePage {

    private String title;

    private String content;
}
