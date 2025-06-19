package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.Article;
import com.admin.module.system.query.ArticleQuery;
import com.admin.module.system.service.ArticleService;
import com.admin.module.system.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文章管理")
@RequestMapping("/article")
@RequiredArgsConstructor
@RestController
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章分页列表")
    @GetMapping("page")
    public PageResult<ArticleVO> selectArticlePage(ArticleQuery articleQuery) {
            IPage<ArticleVO> articleIPage = articleService.selectArticlePage(articleQuery);
            return PageResult.success(articleIPage);
    }

    @Operation(summary = "新增文章")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('article:add')")
    @PostMapping("/save")
    public ResultVO<Boolean> saveArticle(@RequestBody Article article) {
        boolean result = articleService.saveArticle(article);
        return ResultVO.success(result);
    }


    @Operation(summary = "获取文章详情")
    @GetMapping("/detail/{id}")
    public ResultVO<Article> getArticleDetail(@PathVariable("id") Long id) {
        Article article = articleService.getById(id);
        return ResultVO.success(article);
    }


    @Operation(summary = "修改文章")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('article:edit')")
    @PutMapping("/update")
    public ResultVO<Boolean> updateArticle(@RequestBody Article article) {
        boolean result = articleService.updateArticle(article);
        return ResultVO.success(result);
    }


    @Operation(summary = "删除文章")
    @PreAuthorize("@pms.hasPerm('article:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteArticle(@RequestBody List<Long> ids) {
        boolean result = articleService.deleteArticle(ids);
        return ResultVO.success(result);
    }


    @Operation(summary = "门户首页查看文章")
    @GetMapping("/portal/read/{articleId}")
    public ResultVO<ArticleVO> readArticle(@PathVariable("articleId") Long articleId) {
        ArticleVO articleVO = articleService.readArticle(articleId);
        return ResultVO.success(articleVO);
    }
}
