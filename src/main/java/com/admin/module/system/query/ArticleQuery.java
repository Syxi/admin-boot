package com.admin.module.system.query;


import com.admin.common.base.BasePage;
import lombok.Data;

@Data
public class ArticleQuery extends BasePage {

    private String title;

    private String categoryName;

    private Integer publish;
}
