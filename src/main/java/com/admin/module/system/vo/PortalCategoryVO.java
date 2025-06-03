package com.admin.module.system.vo;

import com.admin.module.system.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 门户首页文章分类
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PortalCategoryVO {

    private Long id;

    private String value;

    private String label;

    private List<Article> articleList;
}
