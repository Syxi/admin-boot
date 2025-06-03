package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.Article;
import com.admin.module.system.query.ArticleQuery;
import com.admin.module.system.vo.ArticleVO;

import java.util.List;

/**
* @author sy
* @description 针对表【t_article(文章表)】的数据库操作Service
* @createDate 2024-07-26 14:54:53
*/
public interface ArticleService extends IService<Article> {

    /**
     * 文章分页列表
     * @param articleQuery
     * @return
     */
    IPage<ArticleVO> selectArticlePage(ArticleQuery articleQuery);

    /**
     * 新增文章
     * @param article
     * @return
     */
    boolean saveArticle(Article article);

    /**
     * 更新文章
     * @param article
     * @return
     */
    boolean updateArticle(Article article);

    /**
     * 删除文章
     * @param ids
     * @return
     */
    boolean deleteArticle(List<Long> ids);

    /**
     * 门户首页阅读文章
     * @param articleId
     * @return
     */
    ArticleVO readArticle(Long articleId);
}
