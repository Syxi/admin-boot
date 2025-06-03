package com.admin.module.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.admin.module.system.entity.Category;
import com.admin.module.system.query.CategoryQuery;
import com.admin.module.system.vo.CategoryOption;
import com.admin.module.system.vo.PortalCategoryVO;

import java.util.List;

/**
* @author sy
* @description 针对表【t_category(文章分类表)】的数据库操作Service
* @createDate 2024-07-26 14:54:53
*/
public interface CategoryService extends IService<Category> {

    /**
     * 文章分类列表
     * @param categoryQuery
     * @return
     */
    IPage<Category> selectCategoryPage(CategoryQuery categoryQuery);

    /**
     * 新增文章分类
     * @param category
     * @return
     */
    boolean saveCategory(Category category);

    /**
     * 获取文章分类详情
     * @param categoryId
     * @return
     */
    Category getCategoryDetail(Long categoryId);

    /**
     * 编辑文章分类
     * @param category
     * @return
     */
    boolean updateCategory(Category category);

    /**
     * 删除文章分类
     * @param categoryIds
     * @return
     */
    boolean deleteCategory(List<Long> categoryIds);


    /**
     * 文章分类下拉选项
     * @return
     */
    List<CategoryOption> selectCategoryList();


    /**
     * 门户首页文章分类
     * @return
     */
    List<PortalCategoryVO> selectPortalCategoryList();
}
