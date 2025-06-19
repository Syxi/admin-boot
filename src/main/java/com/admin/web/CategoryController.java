package com.admin.web;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.admin.common.annotation.NoRepeatSubmit;
import com.admin.common.result.PageResult;
import com.admin.common.result.ResultVO;
import com.admin.module.system.entity.Category;
import com.admin.module.system.query.CategoryQuery;
import com.admin.module.system.service.CategoryService;
import com.admin.module.system.vo.CategoryOption;
import com.admin.module.system.vo.PortalCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文章分类")
@RequestMapping("/category")
@RequiredArgsConstructor
@RestController
public class CategoryController {

    private final CategoryService categoryService;


    @Operation(summary = "文章分类分页列表")
    @GetMapping("/page")
    public PageResult<Category> selectCategoryPage(CategoryQuery categoryQuery) {
        IPage<Category> categoryIPage = categoryService.selectCategoryPage(categoryQuery);
        return PageResult.success(categoryIPage);
    }


    @Operation(summary = "新增文章分类")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('category:add')")
    @PostMapping("/save")
    public ResultVO<Boolean> saveCategory(@RequestBody Category category) {
        boolean result = categoryService.saveCategory(category);
        return ResultVO.success(result);
    }

    @Operation(summary = "获取文章分类详情")
    @GetMapping("/detail/{categoryId}")
    public ResultVO<Category> getCategoryDetail(@PathVariable("categoryId") Long categoryId) {
        Category category = categoryService.getCategoryDetail(categoryId);
        return ResultVO.success(category);
    }

    @Operation(summary = "编辑文章分类")
    @NoRepeatSubmit
    @PreAuthorize("@pms.hasPerm('category:edit')")
    @PutMapping("/update")
    public ResultVO<Boolean> updateCategory(@RequestBody Category category) {
        boolean result = categoryService.updateCategory(category);
        return ResultVO.success(result);
    }

    @Operation(summary = "删除文章分类")
    @PreAuthorize("@pms.hasPerm('category:delete')")
    @DeleteMapping("/delete")
    public ResultVO<Boolean> deleteCategory(@RequestBody List<Long> categoryIds) {
        boolean result = categoryService.deleteCategory(categoryIds);
        return ResultVO.success(result);
    }

    @Operation(summary = "文章分类下拉选项")
    @GetMapping("/categoryList")
    public ResultVO<List<CategoryOption>> selectCategoryList() {
        List<CategoryOption> categoryList = categoryService.selectCategoryList();
        return ResultVO.success(categoryList);
    }


    @Operation(summary = "门户首页文章分类")
    @GetMapping("/portal/categoryList")
    public ResultVO<List<PortalCategoryVO>> selectPortalCategoryList() {
        List<PortalCategoryVO> portalCategoryVOS = categoryService.selectPortalCategoryList();
        return ResultVO.success(portalCategoryVOS);
    }



}
