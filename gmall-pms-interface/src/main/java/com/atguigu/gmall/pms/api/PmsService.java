package com.atguigu.gmall.pms.api;

import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.BaseGroupVO;
import com.atguigu.gmall.pms.vo.CategoryVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

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

    /**
     * "详情查询"
     */
    @GetMapping("pms/spuinfo/info/{id}")
     Resp<SpuInfoEntity> queryspuinfo(@PathVariable("id") Long id);

    /**
     *   查询一级分类
     * @param parentCid
     * @param level
     * @return
     */
    @GetMapping("pms/category")
    Resp<List<CategoryEntity>> queryCategoryByPidOrLevel(@RequestParam(value = "parentCid", required = false) Long parentCid, @RequestParam(value = "level", defaultValue = "0") Integer level);

    /**
     *      查询二级三级分类
     * @param parentCid
     * @return
     */
    @GetMapping("pms/category/{pid}")
     Resp<List<CategoryVO>> queryCategoryByPidCategoryVO(@PathVariable(value = "pid", required = true) Long parentCid);
    /**
     * 详情查询
     */
    @GetMapping("pms/skuinfo/info/{skuId}")
    Resp<SkuInfoEntity> querySkuinfoById(@PathVariable("skuId") Long skuId);
    /**
     * 查询图片列表
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuimages/{skuId}")
    Resp<List<SkuImagesEntity>> querySkuImagesBySkiId(@PathVariable("skuId")Long skuId);
    /**
     * 查询销售属性
     * @param spuId
     * @return
     */
    @GetMapping("pms/skusaleattrvalue/{spuId}")
    Resp<List<SkuSaleAttrValueEntity>> querySkuSaleAttrValueBySpuId(@PathVariable("spuId") Long spuId);
    /**
     * 海报信息
     */
    @GetMapping("pms/spuinfodesc/info/{spuId}")
    Resp<SpuInfoDescEntity> querySpuInfoDesc(@PathVariable("spuId") Long spuId);
    /**
     *     查询组及组下规格参数
     * @param cid
     * @param spuId
     * @return
     */
    @GetMapping("pms/attrgroup/item/group/{cid}/{spuId}")
    Resp<List<BaseGroupVO>> queryBaseGroupVOByCateIdAndSpuId(@PathVariable("cid") Long cid, @PathVariable("spuId") Long spuId);
}
