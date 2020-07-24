package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.vo.CategoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.service.CategoryService;


/**
 * 商品三级分类
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-07-11 14:15:26
 */
@Api(tags = "商品三级分类 管理")
@RestController
@RequestMapping("pms/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("{pid}")
    public Resp<List<CategoryVO>> queryCategoryByPidCategoryVO(@PathVariable(value = "pid", required = true) Long parentCid){
        List<CategoryVO> categoryVOS = categoryService.queryCategoryVO(parentCid);
        return Resp.ok(categoryVOS);
    }


    /**
     *   查询一级分类
     * @param parentCid
     * @param level
     * @return
     */
    @GetMapping
    public Resp<List<CategoryEntity>> queryCategoryByPidOrLevel(@RequestParam(value = "parentCid", required = false) Long parentCid,
                                                                @RequestParam(value = "level", defaultValue = "0") Integer level) {
        // 如果没传level,则level默认=0,即查询全部
        List<CategoryEntity> categories = categoryService.list(new LambdaQueryWrapper<CategoryEntity>()
                .eq(level != 0, CategoryEntity::getCatLevel, level)
                .eq(parentCid != null, CategoryEntity::getParentCid, parentCid));
        return Resp.ok(categories);
//        QueryWrapper<CategoryEntity> wrapper = new QueryWrapper<>();
//        if (level!=null){
//            wrapper.eq("cat_level",level);
//        }
//        if (parentCid!=null){
//            wrapper.eq("parent_cid",parentCid);
//        }
//        List<CategoryEntity> categoryEntities = categoryService.list(wrapper);
//        return Resp.ok(categoryEntities);
    }


    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:category:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = categoryService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{catId}")
    @PreAuthorize("hasAuthority('pms:category:info')")
    public Resp<CategoryEntity> info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return Resp.ok(category);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:category:save')")
    public Resp<Object> save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:category:update')")
    public Resp<Object> update(@RequestBody CategoryEntity category) {
        categoryService.updateById(category);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:category:delete')")
    public Resp<Object> delete(@RequestBody Long[] catIds) {
        categoryService.removeByIds(Arrays.asList(catIds));

        return Resp.ok(null);
    }

}
