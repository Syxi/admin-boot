package com.admin.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.admin.common.enums.PublishEnum;
import com.admin.common.exception.CustomException;
import com.admin.module.system.vo.CategoryOption;
import com.admin.module.system.vo.PortalCategoryVO;
import com.admin.module.system.entity.Article;
import com.admin.module.system.entity.Category;
import com.admin.module.system.mapper.CategoryMapper;
import com.admin.module.system.query.CategoryQuery;
import com.admin.module.system.service.ArticleService;
import com.admin.module.system.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author sy
* @description 针对表【t_category(文章分类表)】的数据库操作Service实现
* @createDate 2024-07-26 14:54:53
*/
@Slf4j
@RequiredArgsConstructor
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService{

    private final ArticleService articleService;

    /**
     * 文章分类列表
     *
     * @param categoryQuery
     * @return
     */
    @Override
    public IPage<Category> selectCategoryPage(CategoryQuery categoryQuery) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>();
        if (StringUtils.isNotBlank(categoryQuery.getCategoryName())) {
            wrapper.like(Category::getCategoryName, categoryQuery.getCategoryName());
        }
        wrapper.orderByAsc(Category::getSort);

        IPage<Category> page = new Page<>(categoryQuery.getPage(), categoryQuery.getLimit());
        IPage<Category> iPage = this.page(page, wrapper);
        return iPage;
    }

    /**
     * 新增文章分类
     *
     * @param category
     * @return
     */
    @Override
    public boolean saveCategory(Category category) {
        this.checkCategoryName(category);
        return this.save(category);
    }


    /**
     * 检查文章分类名称是否已存在
     * @param category
     * @return
     */
    private void checkCategoryName(Category category) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getCategoryName, category.getCategoryName());
        if (category.getCategoryId() != null) {
            wrapper.ne(Category::getCategoryId, category.getCategoryId());
        }
        boolean result = this.exists(wrapper);

        if (result) {
            log.error("文章分类名称已存在：{}", category.getCategoryName());
            throw new CustomException("文章分类名称已存在");
        }
    }

    /**
     * 获取文章分类详情
     *
     * @param categoryId
     * @return
     */
    @Override
    public Category getCategoryDetail(Long categoryId) {
        return this.getById(categoryId);
    }

    /**
     * 编辑文章分类
     *
     * @param category
     * @return
     */
    @Override
    public boolean updateCategory(Category category) {
        this.checkCategoryName(category);
        return this.updateById(category);
    }

    /**
     * 删除文章分类
     *
     * @param categoryIds
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteCategory(List<Long> categoryIds) {
        return this.removeBatchByIds(categoryIds);
    }

    /**
     * 文章分类选项
     *
     * @return
     */
    @Override
    public List<CategoryOption> selectCategoryList() {
        List<CategoryOption> option = this.list().stream()
                .sorted(Comparator.comparing(Category::getSort))
                .map(category -> this.convertCategoryOption(category))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(option)) {
            return Collections.emptyList();
        }

        return option;
    }

    /**
     * Category  转换成 CategoryOption
     * @param category
     * @return CategoryOption
     */
    private CategoryOption convertCategoryOption(Category category) {
        CategoryOption categoryOption = new CategoryOption();
        categoryOption.setId(category.getCategoryId());
        categoryOption.setValue(category.getCategoryName());
        categoryOption.setLabel(category.getCategoryName());
        return categoryOption;
    }


    /**
     * 门户首页文章分类
     *
     * @return
     */
    @Override
    public List<PortalCategoryVO> selectPortalCategoryList() {
        List<PortalCategoryVO> option = this.list().stream()
                .sorted(Comparator.comparing(Category::getSort))
                .map(category -> this.convertPortalCategoryVO(category))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(option)) {
            return Collections.emptyList();
        }

        return option;
    }

    /**
     * Category  转换成 PortalCategoryVO
     * @param category
     * @return PortalCategoryVO
     */
    private PortalCategoryVO convertPortalCategoryVO(Category category) {
        PortalCategoryVO portalCategoryVO = new PortalCategoryVO();
        portalCategoryVO.setId(category.getCategoryId());
        portalCategoryVO.setValue(category.getCategoryName());
        portalCategoryVO.setLabel(category.getCategoryName());

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getCategoryName, category.getCategoryName());
        wrapper.eq(Article::getPublish, PublishEnum.IS_PUBLISH.getValue());
        wrapper.orderByAsc(Article::getSort);
        List<Article> articles = articleService.list(wrapper);
        portalCategoryVO.setArticleList(articles);
        return portalCategoryVO;
    }

}




