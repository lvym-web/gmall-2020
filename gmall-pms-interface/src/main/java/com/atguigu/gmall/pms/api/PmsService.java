package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface PmsService {
    /**
     * 分页查询spu
     */
    @PostMapping("pms/spuinfo/page")
    Resp<List<SpuInfoEntity>> page(@RequestBody QueryCondition queryCondition);

    /**
     * 根据spuId查询spu下的sku (skuId pic price title brandId)
     */
    @GetMapping("pms/skuinfo/{spuId}")
    Resp<List<SkuInfoEntity>> querySkuBySpuId(@PathVariable("spuId") Long spuId);

    /**
     * 根据brandId查询brand
     */
    @GetMapping("pms/brand/info/{brandId}")
    Resp<BrandEntity> queryBrandById(@PathVariable("brandId") Long brandId);

    /**
     * 根据categoryId查询category
     */
    @GetMapping("pms/category/info/{catId}")
    Resp<CategoryEntity> queryCategoryById(@PathVariable("catId") Long catId);

    /**
     * 根据spuId查询搜索属性和值
     */
    @GetMapping("pms/productattrvalue/{spuId}")
    Resp<List<ProductAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId") Long spuId);

}
