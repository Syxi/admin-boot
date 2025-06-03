package com.admin.module.system.service.impl;

import com.admin.module.system.entity.Article;
import com.admin.module.system.mapper.ArticleMapper;
import com.admin.module.system.query.ArticleQuery;
import com.admin.module.system.service.ArticleService;
import com.admin.module.system.vo.ArticleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author sy
* @description 针对表【t_article(文章表)】的数据库操作Service实现
* @createDate 2024-07-26 14:54:53
*/
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService{

    /**
     * 文章分页列表
     *
     * @param articleQuery
     * @return
     */
    @Override
    public IPage<ArticleVO> selectArticlePage(ArticleQuery articleQuery) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(articleQuery.getTitle())) {
            wrapper.like(Article::getTitle, articleQuery.getTitle());
        }
        if (StringUtils.isNotBlank(articleQuery.getCategoryName())) {
            wrapper.like(Article::getCategoryName, articleQuery.getCategoryName());
        }
        if (articleQuery.getPublish() != null) {
            wrapper.eq(Article::getPublish, articleQuery.getPublish());
        }
        wrapper.orderByAsc(Article::getSort);
        IPage<Article> page = new Page<>(articleQuery.getPage(), articleQuery.getLimit());
        IPage<Article> articlePage = this.page(page, wrapper);


        IPage<ArticleVO> articleVOIPage = articlePage.convert(article -> {
            ArticleVO articleVO = this.convertToArticleVO(article);
            return articleVO;
        });

        return articleVOIPage;
    }


    private ArticleVO convertToArticleVO(Article article) {
        ArticleVO articleVO = new ArticleVO();
        articleVO.setArticleId(article.getArticleId());
        articleVO.setCategoryName(article.getCategoryName());
        articleVO.setTitle(article.getTitle());
        articleVO.setAuthor(article.getAuthor());
        articleVO.setAvatar(article.getAvatar());
        articleVO.setIntroduction(article.getIntroduction());
        articleVO.setReadCount(article.getReadCount());
        articleVO.setPublish(article.getPublish());
        articleVO.setTop(article.getTop());
        articleVO.setSort(article.getSort());
        articleVO.setCreateTime(article.getCreateTime());
        articleVO.setUpdateTime(article.getUpdateTime());
        articleVO.setContent(article.getContent());


        return articleVO;
    }

    /**
     * 新增文章
     *
     * @param article
     * @return
     */
    @Override
    public boolean saveArticle(Article article) {
        return this.save(article);
    }



    /**
     * 更新文章
     *
     * @param article
     * @return
     */
    @Override
    public boolean updateArticle(Article article) {
        return this.updateById(article);
    }

    /**
     * 删除文章
     *
     * @param ids
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteArticle(List<Long> ids) {
        return this.removeByIds(ids);
    }

    /**
     * 门户首页阅读文章
     *
     * @param articleId
     * @return
     */
    @Override
    public ArticleVO readArticle(Long articleId) {
        Article article = this.getById(articleId);
        ArticleVO articleVO = this.convertToArticleVO(article);
        return articleVO;
    }
}




